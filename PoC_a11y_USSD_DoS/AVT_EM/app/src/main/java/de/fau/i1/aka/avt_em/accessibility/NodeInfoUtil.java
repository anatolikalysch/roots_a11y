package de.fau.i1.aka.avt_em.accessibility;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;


public class NodeInfoUtil
{
    public static void click(AccessibilityNodeInfo node)
    {
        if (!node.isClickable())
        {
            AccessibilityNodeInfo parent = node.getParent();
            if (parent != null)
            {
                click(parent);
                parent.recycle();
            }
        }
        else
        {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    public static boolean eventTypeIs(AccessibilityEvent event, int... types)
    {
        int eventType = event.getEventType();
        for (int type : types)
        {
            if (eventType == type)
            {
                return true;
            }
        }
        return false;
    }

    public static boolean packageNameIs(AccessibilityEvent event, String... packages)
    {
        CharSequence nodePackageName = event.getPackageName();
        if (nodePackageName == null)
        {
            return false;
        }

        for (String packageName : packages)
        {
            if (nodePackageName.equals(packageName))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean packageNameIs(AccessibilityNodeInfo node, String... packages)
    {
        CharSequence nodePackageName = node.getPackageName();
        if (nodePackageName == null)
        {
            return false;
        }

        for (String packageName : packages)
        {
            if (nodePackageName.equals(packageName))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean packageNameContains(AccessibilityEvent event, String... packages)
    {
        CharSequence nodePackageNameCharSequence = event.getPackageName();
        if (nodePackageNameCharSequence == null)
        {
            return false;
        }

        String nodePackageName = nodePackageNameCharSequence.toString();
        for (String packageName : packages)
        {
            if (packageName != null && nodePackageName.contains(packageName))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean textIs(AccessibilityNodeInfo node, String... texts)
    {
        CharSequence nodeText = node.getText();
        if (nodeText == null)
        {
            return false;
        }

        for (String text : texts)
        {
            if (nodeText.equals(text))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean contentDescriptionIs(AccessibilityNodeInfo node, String... descriptions)
    {
        CharSequence nodeText = node.getContentDescription();
        if (nodeText == null)
        {
            return false;
        }

        for (String text : descriptions)
        {
            if (nodeText.equals(text))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean classNameIs(AccessibilityNodeInfo node, String... classes)
    {
        CharSequence nodeClassName = node.getClassName();
        if (nodeClassName == null)
        {
            return false;
        }

        for (String className : classes)
        {
            if (nodeClassName.equals(className))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean containsNodeById(AccessibilityNodeInfo root, String id)
    {
        List<AccessibilityNodeInfo> results = root.findAccessibilityNodeInfosByViewId(id);
        if (results.isEmpty())
        {
            return false;
        }
        recycle(results);
        return true;
    }

    public static AccessibilityNodeInfo findNodeById(AccessibilityNodeInfo root, String id)
    {
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(id);

        return getUniqueNode(nodes);
    }

    public static AccessibilityNodeInfo findNodeByText(AccessibilityNodeInfo root, String text)
    {
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(text);

        return getUniqueNode(nodes);
    }

    private static AccessibilityNodeInfo getUniqueNode(List<AccessibilityNodeInfo> nodes)
    {
        if (nodes.size() == 1)
        {
            return nodes.get(0);
        }
        else
        {
            recycle(nodes);
            return null;
        }
    }

    public static void recycle(List<AccessibilityNodeInfo> nodes)
    {
        for (AccessibilityNodeInfo node : nodes)
        {
            node.recycle();
        }
    }

    public static void print(AccessibilityEvent event)
    {
        AccessibilityNodeInfo root = event.getSource();
        if (root != null)
        {
            print(root, 0);
            root.recycle();
        }
    }

    public static void print(AccessibilityNodeInfo node)
    {
        if (node != null)
        {
            print(node, 0);
        }
    }

    private static void print(AccessibilityNodeInfo nodeInfo, int deep)
    {
        CharSequence className = nodeInfo.getClassName();
        if (className == null) className = "";

        CharSequence text = nodeInfo.getText();
        if (text == null) text = "";

        CharSequence contentDescription = nodeInfo.getContentDescription();
        if (contentDescription == null) text = "";

        String id = nodeInfo.getViewIdResourceName();
        if (id == null) id = "";

        CharSequence packageName = nodeInfo.getPackageName();
        if (packageName == null) packageName = "";

        boolean clickable = nodeInfo.isClickable();
        boolean checkable = nodeInfo.isCheckable();
        boolean scrollable = nodeInfo.isScrollable();

        String attributes = clickable ? "t/" : "f/";
        attributes += checkable ? "t/" : "f/";
        attributes += scrollable ? "t" : "f";

        if (clickable || checkable || scrollable)
        {
            Log.e("A11Y", "Deep " + deep + ": " + packageName + "\t" + className + "\t" + text + "\t" + contentDescription + "\t" + id + "\t" + attributes);
        }
        else
        {
            Log.i("A11Y", "Deep " + deep + ": " + packageName + "\t" + className + "\t" + text + "\t" + contentDescription + "\t" + id + "\t" + attributes);
        }

        int newDeep = deep + 1;

        for (int i = 0; i < nodeInfo.getChildCount(); i++)
        {
            AccessibilityNodeInfo child = nodeInfo.getChild(i);
            if (child != null)
            {
                print(child, newDeep);
                child.recycle();
            }
        }
    }

    public static String getEventTypeName(AccessibilityEvent event)
    {
        switch (event.getEventType())
        {
            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                return "TYPE_ANNOUNCEMENT";
            case AccessibilityEvent.TYPE_ASSIST_READING_CONTEXT:
                return "TYPE_ASSIST_READING_CONTEXT";
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_END:
                return "TYPE_GESTURE_DETECTION_END";
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
                return "TYPE_GESTURE_DETECTION_START";
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                return "TYPE_NOTIFICATION_STATE_CHANGED";
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                return "TYPE_TOUCH_EXPLORATION_GESTURE_END";
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                return "TYPE_TOUCH_EXPLORATION_GESTURE_START";
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_END:
                return "TYPE_TOUCH_INTERACTION_END";
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_START:
                return "TYPE_TOUCH_INTERACTION_START";
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED:
                return "TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED";
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
                return "TYPE_VIEW_ACCESSIBILITY_FOCUSED";
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                return "TYPE_VIEW_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED:
                return "TYPE_VIEW_CONTEXT_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                return "TYPE_VIEW_FOCUSED";
            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
                return "TYPE_VIEW_HOVER_ENTER";
            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
                return "TYPE_VIEW_HOVER_EXIT";
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                return "TYPE_VIEW_LONG_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                return "TYPE_VIEW_SCROLLED";
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                return "TYPE_VIEW_SELECTED";
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                return "TYPE_VIEW_TEXT_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                return "TYPE_VIEW_TEXT_SELECTION_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY:
                return "TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY";
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                return "TYPE_WINDOW_CONTENT_CHANGED";
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                return "TYPE_WINDOW_STATE_CHANGED";
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                return "TYPE_WINDOWS_CHANGED";
            default:
                return "NO_TYPE";
        }
    }
}
