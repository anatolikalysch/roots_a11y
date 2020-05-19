ISSUE DESCRIPTION:

An attacker can misuse Accessibility Services to sniff the credentials of any Android app, including the lock screen. Besides the lock screen, user passwords from banking apps, smarthome apps, social media and more are affected. This document describes an attack vector that can be used to read credentials from password fields (and other TextViews) on non-rooted phones, just by installing an Accessibility Service from unprivileged apps. Specifically, we leverage a bug in the TextView's "PasswordTransformationMethod" to record user interaction via the AccessibilityEvents "TYPE_VIEW_TEXT_SELECTION_CHANGED" and "TYPE_VIEW_TEXT_CHANGED".

The reported bug contradicts Google's own documentation on Accessibility Services, stating that no sensitive data of a TextView (like password characters) can be received by Accessibility Events. We tested our PoC on several devices (see below) and Android Emulators with API levels 23, 24, 25 and 26, and found all of them to be vulnerable for the bug presented here.

SHORT BUG DESCRIPTION:

In short, with the system setting "Settings.System.TEXT_SHOW_PASSWORD" enabled (which is the default on stock Android), the "PasswordTransformationMethod" of a TextView leaks password characters to any AccessibilityEvent created up to 1.5 seconds after the character was entered.

ATTACK VECTORS:

=============================================================
{1} AccessibilityEvent 0x2000 (=8192) TYPE_VIEW_TEXT_SELECTION_CHANGED and the setting "Settings.System.TEXT_SHOW_PASSWORD" enabled (default).

The setting "Settings.System.TEXT_SHOW_PASSWORD" shows the last character entered by the user in plain text for usability. The text of an AccessibilityEvent of type TYPE_VIEW_TEXT_SELECTION_CHANGED is set accordingly with the last character being present in plain text. Since every entered character generates an AccessibilityEvent of type TYPE_VIEW_TEXT_SELECTION_CHANGED, an attacker can collect these events, extract their text through the getText() method and easily piece together the password (as well as the username). This affects any Android app, including system apps like the lock screen (if protected through a text or number, not a pattern or fingerprint), and third party apps, as long as they do not disable the AccessibilityEvent TYPE_VIEW_TEXT_SELECTION_CHANGED.

A user can not detect the attacking event, as no UI elements are manipulated. The AccessibilityService that has read the credentials is able to exfiltrate these through an internet connection.

{2} AccessibilityEvent 0x10 (=16) TYPE_VIEW_TEXT_CHANGED and the setting "Settings.System.TEXT_SHOW_PASSWORD" enabled (default).

We conjecture there is a race condition between the passwordTransformationMethod and the fired AccessibilityEvent TYPE_VIEW_TEXT_CHANGED. Contrary to {1}, the password transformation method of the TextView conceals every character for this type of AccessibilityEvent if the password is typed slowly. Typing a password at a fast rate, however, introduces a race condition where the second last character is shown in plain text while the others are obscured through the password transformation method. An attacker can thereby sniff any fast typed credentials except for the last character through this event.
=============================================================
NOTE on {1} and {2}: The system setting Settings.System.TEXT_SHOW_PASSWORD can be enabled by the AccessibilityService if it was disabled. See our PoC.
=============================================================

DESCRIPTION INCLUDING SOURCE FILES AND FUNCTIONS:
=============================================================
{1} and {2} rely on the same bug, which introduces a runtime condition for the visibility of the recently entered password character:

An AccessibilityEvent is created every time an accessibility relevant action is performed on the UI. This can be, for example, the input of new characters in a password EditText / TextView. AccessibilityEvents are composed of several fields, the most important is the Charsequence mText, which can contain a textual representation of data relevant for this event. In the case of a TextView, this will contain the text of the TextView. If the TextView is a password, however, the text will be transformed to be DOT characters instead of plain text. An AccessibilityEvents text is set by the creator via setText() and queried by an AccessibilityService vie getText().

The main problem is the text of the AccessibilityEvent TYPE_VIEW_TEXT_SELECTION_CHANGED and TYPE_VIEW_TEXT_CHANGED may contain the last character in plain text, not as a DOT character, just like in the UI for usability. The text value of such an AccessibilityEvent is set by the EditText.java class, which extends TextView.java. The TextView holds two representations of the current TextView contents, CharSequence mText and CharSequence mTransformed. The former is the plain text representation, while the latter is a version transformed by the TextViews TransformationMethod. Passwords have their own TransformationMethod, namely PasswordTransformationMethod.java, which is responsible for concealing all password characters, or if the setting TEXT_SHOW_PASSWORD is enabled, concealing all but the last character.

If the setting TEXT_SHOW_PASSWORD is enabled the last password character remains visible for 1.5 seconds:

=============================================================
frameworks/base/core/java/android/text/method/PasswordTransformationMethod.java
33
34 public class PasswordTransformationMethod
35 implements TransformationMethod, TextWatcher
36 {
...
75     public void onTextChanged(CharSequence s, int start, // called if text changes. For password input this boils down to insertion or deletion of one character; CharSequence s -> the text after the change; int start -> index indicating where the change begins inside the CharSequence;
76                               int before, int count) { // int before -> not relevant here; int count - how many characters were changed
        ...
101             int pref = TextKeyListener.getInstance().getPrefs(v.getContext()); // the prefs basically consist of the following flags: AUTO_CAP | AUTO_TEXT | AUTO_PERIOD | SHOW_PASSWORD;
102             if ((pref & TextKeyListener.SHOW_PASSWORD) != 0) { // this handles our special case when TEXT_SHOW_PASSWORD is set;
103                 if (count > 0) {
104                     removeVisibleSpans(sp); // Spannable sp = (Spannable) s; sp is the parameter CharSequence s casted to a Spannable;
105
106                     if (count == 1) { // this is true if one additional character was inserted, e.g. the user typed a character
107                         sp.setSpan(new Visible(sp, this), start, start + count,
108                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // make the characters between 'start' and 'count' visible -> usually only one character, as start and count describe the length of the changes happening to the TextView; The length of the visibility is 1.5 seconds -> hardcoded in the Visible class:
109                     }
110                 }
111             }
112         }
113     }
...
235     private static class Visible
236     extends Handler
237     implements UpdateLayout, Runnable
238     {
239         public Visible(Spannable sp, PasswordTransformationMethod ptm) {
240             mText = sp;
241             mTransformer = ptm;
242             postAtTime(this, SystemClock.uptimeMillis() + 1500); // after 1.5 seconds execute the run() method;
243         }
244
245         public void run() {
246             mText.removeSpan(this); // the run() method effectively removes the visible spannable, which in our use case usually is the last character of the password that was visible and now will be replaced with a 'DOT' character like the rest of the password characters.
247         }
248
249         private Spannable mText;
250         private PasswordTransformationMethod mTransformer;
251     }
...
266 }
=============================================================

This password transformation method works well if the password is shown in an EditText, as the user can see the previously entered character for a brief amount of time. However, with an active AccessibilityService, every character entered by the user generates several new AccessibilityEvents. EditText and TextView related AccessibilityEvents have the Text field set to the contents of the generating EditText/TextView:

=============================================================
frameworks/base/core/java/android/widget/TextView.java
...
303 @RemoteView
304 public class TextView extends View implements ViewTreeObserver.OnPreDrawListener {
...
5255     private void setText(CharSequence text, BufferType type,
5256                          boolean notifyBefore, int oldlen) {
5257         mTextFromResource = false;
5258         if (text == null) {
5259             text = "";
5260         }
         ...
5347         mBufferType = type;
5348         mText = text;
5349
5350         if (mTransformation == null) {
5351             mTransformed = text; // if there is no transformation method this is not a password field
5352         } else {
5353             mTransformed = mTransformation.getTransformation(text, this);
5354         }
5355
5356         final int textLength = text.length();
         ...
5409     }
...
9261     /**
9262      * This method is called when the selection has changed, in case any
9263      * subclasses would like to know.
9264      *
9265      * @param selStart The new selection start location.
9266      * @param selEnd The new selection end location.
9267      */
9268     protected void onSelectionChanged(int selStart, int selEnd) {
9269         sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED); // This is calles when an AccessibilityEvent is created.
9270     }
...
10712     /** @hide */
10713     @Override
10714     public void sendAccessibilityEventInternal(int eventType) { // This populates an AccessibilityEvent.
10715         if (eventType == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED && mEditor != null) {
10716             mEditor.mProcessTextIntentActionsHandler.initializeAccessibilityActions();
10717         }
10718
10719         // Do not send scroll events since first they are not interesting for
10720         // accessibility and second such events a generated too frequently.
10721         // For details see the implementation of bringTextIntoView().
10722         if (eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
10723             return;
10724         }
10725         super.sendAccessibilityEventInternal(eventType);
10726     }
10727
10728     /**
10729      * Returns the text that should be exposed to accessibility services.
10730      * <p>
10731      * This approximates what is displayed visually. If the user has specified
10732      * that accessibility services should speak passwords, this method will
10733      * bypass any password transformation method and return unobscured text.
10734      *
10735      * @return the text that should be exposed to accessibility services, may
10736      *         be {@code null} if no text is set
10737      */
10738     @Nullable
10739     private CharSequence getTextForAccessibility() { // most relevant: This method decides which text is set via a setText() method of an AccessibilityEvent. This text can be then queried by anyone receiving the AccessibilityEvent.
10740         // If the text is empty, we must be showing the hint text.
10741         if (TextUtils.isEmpty(mText)) {
10742             return mHint;
10743         }
10744
10745         // Otherwise, return whatever text is being displayed.
10746         return mTransformed; // In line 5353 we see, that mTransformed is whatever the TransformationMethod returned. As we have seen before if TEXT_SHOW_PASSWORD is set, the last character is made visible for 1.5 seconds.
10747     }
...
=============================================================

This introduces a 'run time condition', as there is a 1.5 sec window in which all TextView related AccessibilityEvents of type AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED will contain the last entered password character in plain text. The PasswordTransformationMethod will conceal the plain text character only after 1.5 sec, however, the event AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED is generated and queries the CharSequence mTransformed right at the beginning of this time frame, thereby always containing the last entered password character in plain text.

The event TYPE_VIEW_TEXT_CHANGED may be generated and query mTransformed during this window only if the user has an according typing speed. With passwords this is usually a safe assumption, as they must often be entered multiple times a day.

Additionally, the events TYPE_VIEW_CLICKED or TYPE_VIEW_LONG_CLICKED provide the same access to the contents of a TextView. However they must be periodically generated by the AccessibilityService itself and are not fired automatically. They are mentioned here for completion.

=============================================================
BUILD FINGERPRINTS OF DEVICES

google/hammerhead/hammerhead:5.1/LMY47I/1767468:user/release-keys
google/bullhead/bullhead:7.1.2/N2G48C/4104010:user/release-keys
OnePlus/OnePlus3/OnePlus3:8.0.0/OPR6.170623.013/10250816:user/release-keys
HUAWEI/VNS-L31/HWVNS-H:6.0/HUAWEIVNS-L31/C432B160:user/release-keys


PATCH / FIX
=============================================================
{1}, {2}

All AccessibilityEvents can be fixed with the introduction of an additional variable in the TextView class and returning it as the text value if the underlying TextView is a password:

frameworks/base/core/java/android/widget/TextView.java
--- new ---
     import java.util.Collections;
--- done ---
...
--- new ---
     private static String DOT = "\u2022"; // this is a •
     private CharSequence mPassTransformed;
--- done ---
...
5255     private void setText(CharSequence text, BufferType type,
5256                          boolean notifyBefore, int oldlen) {
5257         mTextFromResource = false;
5258         if (text == null) {
5259             text = "";
5260         }
         ...
5347         mBufferType = type;
5348         mText = text;
5349
5350         if (mTransformation == null) {
5351             mTransformed = text;
5352         } else {
5353             mTransformed = mTransformation.getTransformation(text, this);
5354         }
5355
5356         final int textLength = text.length();
5357
--- new ---
Starting java8:
         if (isPassword) {
                mPassTransformed = String.join("", Collections.nCopies(textLength, DOT));
         } else {
                mPassTransformed = mTransformed;
         }

Alternative:
NOTE: For this alternative DOT has to be a char: private static char DOT = '\u2022';
         if (isPassword) {
                char[] dots = new char[textLength];
                Arrays.fill(chars, DOT)
                mPassTransformed = new String(dots);
         } else {
                mPassTransformed = mTransformed;
         }



--- done ---
...
10728     /**
10729      * Returns the text that should be exposed to accessibility services.
10730      * <p>
10731      * This approximates what is displayed visually. If the user has specified
10732      * that accessibility services should speak passwords, this method will
10733      * bypass any password transformation method and return unobscured text.
10734      *
10735      * @return the text that should be exposed to accessibility services, may
10736      *         be {@code null} if no text is set
10737      */
10738     @Nullable
10739     private CharSequence getTextForAccessibility() {
10740         // If the text is empty, we must be showing the hint text.
10741         if (TextUtils.isEmpty(mText)) {
10742             return mHint;
10743         }
--- new ---
          // If the text is a password return the completely censored password, else the transformed text
          if (isPassword) {
                return mPassTransformed;
          } else {
                return mTransformed;
          }
--- done ---
      }
...

=============================================================
ALTERNATIVE FIXES
=============================================================
Alternative ideas for fixes are presented here. If you prefer any of these over the above solution, we can help with providing the necessary code.

 - Censor the text field of the AccessibilityEvent itself if DOT characters should be detected.
This fix would censor the the text of an AccessibilityEvent. In the AccessibilityEvents setText(...) method a check could be performed if some characters are in plain text even through the source of the event returns true for isPassword(). 

 - Another possibility is to have a separate accessibilityGetText() method inside the PasswordTransformationMethod.java and call this method inside the getTextForAccessibility() method instead of just returning mTransformed. accessibilityGetText() would return the same output as getText() with the exception of the Visible, that is spanned over the last character. 
