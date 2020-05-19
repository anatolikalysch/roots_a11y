# roots_a11y

This repository consolidates PoC files for the publication [How Android's UI Security is Undermined by Accessibility](https://faui1-files.cs.fau.de/public/publications/a11y_final_version.pdf).
The PoCs are published in form of code snippets describing the Attack vectors but do not facilitate a working state on their own. Instead they should be integrated into an Android Project to be made workable. Each PoC directory has its own README describing the attack and the culnerability it abuses. We tested the described vulnerabilities and attacks on several Android versions and came to the following conclusions:


| Attack                                                   | 6.0 |	7.0	| 7.1.2	| 8.0 | 8.1 |Possible Countermeasures                          |
|----------------------------------------------------------|-----|------|-------|-----|-----|---------------------------------------------------|
| Accessibility Event Sniffing                             | ✗ |	✓	| ✓	| ✓ |  ✓ | a11y event sanitizing, fingerprint authentication |
| Accessibility Screen Recording                           | ✓ |	✓	| ✓	| ✓| ✓| secure flag and in-app keyboard                   |
| Accessibility-enabled Malicious IME                      | ✓ |	✓	| ✓	| ✓| ✓| in-app keyboard and behavior listener             |
| Accessibility-based Ad Hijacking [2]                     | ✓ |	✓	| ✓	| ✓| ✓| a11y event sanitizing                             |
| Overlay and Accessibility assisted Password Stealing [2] | ✓ |	✓	| ✓	| ✓| ✓| a11y event sanitizing, window punching            |
| Keyboard App Hijacking [2]                               | (✓) |	(✓)	| (✓)	| ✗ | ✗ | in-app keyboard or enforcing Gboard update        |
| Full App Passthrough / Clickable Overlays [3]            | ✓ |	✓	| ✓	| ✓* | ✓* | window punching                                   |
| Partial App Clickable Overlays [1]                       | ✓ |	✓	| ✓	| ✓* | ✓* | window punching                                   |
| Context-aware Clickjacking / Hiding [2]                  | ✓ |	✓	| ✓	| ✓* |✓* | window punching                                   |
| Keystroke Inference [2]                                  | ✓ |	✓	| ✓	| ✗ | ✗ | in-app keyboard and window punching               |                                                        |                             |                                                   |

6.0	7.0	7.1.2	8.0	8.1
# Minimal Project Structure

The a11y service should contain a XML description of the service capabilities as specified [in the developer docs](https://developer.android.com/guide/topics/ui/accessibility/services#service-config) with all the necessary capabilities the service needs to function correctly. Additionally, the accessibility service class should be implementing all of the necessary methods for the service to be compiled -- these a not part of the PoC snippets.

# Countermeasures

PoCs for the countermeasures described in [How Android's UI Security is Undermined by Accessibility](https://faui1-files.cs.fau.de/public/publications/a11y_final_version.pdf) are described in their own respective projects. If you have a countermeasure PoC that should be mentioned here as well contact me with a short description of your project and why it should be mentioned here.

Please note, that I do not take any responsibility about the contents of the here referred projects and the contents are provided by the respective project authors.

## Disabling a11y events

Since any default view element generates
events that can be sniffed by an a11y service, our first countermea-
sure aims at filtering these events explicitly. To prevent a11y events
either custom views can be employed or event propagation can
be disabled. A View object implements the AccessibilityEventSour-
ce interface, which is responsible for sending events to an a11y
service. By overwriting the methods sendAccessibilityEvent and
sendAccessibilityEventUnchecked it is possible to remove the default
propagation for custom views which inherit from View. For already
existing views, however, such as LinearLayout and RelativeLayout,
modifying the source code is not possible. Thus, developers can
use the View.AccessibilityDelegate class to modify the accessibility
behavior of the view. When set, it is used to delegate calls to the
sendAccessibilityEvent* methods to an object the developer controls,
effectively allowing the event generation to be prevented without
modifying the source.

## Using a11y limitations to exclude a11y services

### Behavioral Listeners

While a11y services pose a
powerful tool and are quite adept at simulating user behavior, i.e.,
taking UI actions for the user we found ways to distinguish and
prevent a11y services from taking UI actions if required. Specifically,
a11y services have two major limitations when the ACTION_CLICK is
performed on an UI element. First, no click coordinates are available
from this event contrary to a user generated click that contains
the X and Y coordinates. Checking for the click coordinates can
very well determine if a real user was the initiator. Second, an
a11y service can only trigger OnClickListeners and is unable to
trigger OnTouchListener. This results in app logic encoded in, e.g.,
TouchDown or TouchUp MotionEvents from never being triggered
by a11y services at all.

## Creating an own IME and hardening it

A dedicated keyboard per app, implementing
particular security measures, can counter data leaks through screen
recordings and malicious third-party keyboards alike. While a gen-
eral IME, which the user installs as a separate app, might be a
viable solution to prevent screen recordings, it depends on the
user to take security actions. An app-exclusive IME bound to spe-
cific input fields, e.g., the password input, can provide protection
independently of the user behavior. Such keyboard must first be
secured with the FLAG_SECURE and second employ event filtering,
as explained above. Some apps already have a custom app keyboard
implemented, however, during our vulnerability assessment, we did
not encounter any custom IME that had its a11y events sanitized,
and less than half had the secure flag set.

## Window Punching

Recent advances in Android UI-based attacks
combined a11y services with overlays, by either overlaying the
whole app [9] or its login fields [10]. While deactivating a11y events
would ensure that the user detects the attack, the detection would
happen only after entering credentials when the information leak
already happened. A proactive way to prevent users accidentally
entering input into overlay screen builds on the “window punching”
technique proposed by AlJarrah and Shehab [3]. Using the Instru-
mentation library of the Android SDK to simulate touch events in
random intervals, it can be tested whether input fields are overlayed.
Whenever a touch event is fired and hits a window not owned by
the current app, a SecurityException is thrown by the app.

## Fingerprint API usage

Constitutes an alternative authentication mechanisms, not allowing for credential sniffing, and offering
high usability. The use of Android’s fingerprint API protects against
many a11y-based attacks. There were, however, no apps in our
dataset offering only a fingerprint-based authentication. Naturally
an appropriate device with fingerprint sensor and hardware-backed
key storage is needed, as the API is used in conjunction with the
Android Keystore System to store cryptographic keys on the device.
