/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mini.gui;

import org.mini.gui.event.GActionListener;
import org.mini.gui.event.GFlyListener;
import org.mini.gui.event.GFocusChangeListener;
import org.mini.gui.event.GStateChangeListener;
import org.mini.gui.gscript.Interpreter;
import org.mini.nanovg.Nanovg;

import java.util.TimerTask;

import static org.mini.gui.GToolkit.nvgRGBA;

/**
 * @author gust
 */
abstract public class GObject implements GAttachable {

    //
    public static final int ALIGN_H_FULL = 1;
    public static final int ALIGN_V_FULL = 2;

    public static char ICON_SEARCH = (char) 0x1F50D;
    public static char ICON_CIRCLED_CROSS = 0x2716;
    public static char ICON_CHEVRON_RIGHT = 0xE75E;
    public static char ICON_CHECK = 0x2713;
    public static char ICON_LOGIN = 0xE740;
    public static char ICON_TRASH = 0xE729;
    //
    public static final int LEFT = 0;
    public static final int TOP = 1;
    public static final int WIDTH = 2;
    public static final int HEIGHT = 3;

    volatile static int flush;
    static boolean paintDebug = false;

    /**
     * drag gobject move out of it's boundle
     * if gobject need drag action self ,like select text in textbox ,then it can't dragfly
     * //是否可以被鼠标拖到组件自己之外的坐标处,比如在文本框中选中文本需要拖动，则文本框组件无法dragfly
     */
    protected boolean flyable = false;
    int flyOffsetX, flyOffsetY;

    protected GContainer parent;

    protected float[] boundle = new float[4];

    protected float[] bgColor;
    protected float[] color;
    protected float[] disabledColor;
    protected float[] flyingColor;

    protected float fontSize;

    protected GActionListener actionListener;

    protected GFocusChangeListener focusListener;

    protected GStateChangeListener stateChangeListener;

    protected GFlyListener flyListener;

    protected boolean visible = true;

    protected boolean enable = true;

    protected boolean front = false;

    protected boolean back = false;

    protected boolean fixedLocation = false;

    protected String name;

    protected String text;

    protected Object attachment;//用户自定义数据

    private String cmd;//类似attachment 用于附加String类型用户数据
    protected GLayout layout;

    //脚本触发器
    /**
     * two call formate:
     * framename.fun(1,2)  'assignment Interpreter by parent component name
     * fun(1,2)            'not assignment, it will find the first Interpreter of parents
     */
    private String onClinkScript;
    private String onStateChangeScript;

    /**
     *
     */
    public void init() {

    }

    public void destroy() {
    }


    static synchronized public void flush() {
        flush = 3;
        //in android may flush before paint,so the menu not shown
    }

    public void setFixed(boolean fixed) {
        fixedLocation = fixed;
    }

    public boolean getFixed() {
        return fixedLocation;
    }

    static synchronized public boolean flushReq() {
        if (flush > 0) {
            flush--;
            return true;
        }
        return false;
    }

    public void schedule(TimerTask task, long delay, long period) {
        if (GForm.timer != null) {
            getForm().setActiveListener(active -> {

            });
            GForm.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        task.run();
                    } catch (Exception e) {
                        cancel();//cancel this ,not task
                    }
                }
            }, delay, period);
        }
    }

    public <T extends GObject> T findParentByName(String name) {
        if (name == null) return null;
        if (parent != null) {
            if (name.equals(parent.getName())) {
                return (T) parent;
            } else {
                return parent.findParentByName(name);
            }
        }
        return null;
    }

    boolean paintFlying(long vg, float x, float y) {
        return true;
    }

    public boolean paint(long ctx) {
        return true;
    }

    public void keyEventGlfw(int key, int scanCode, int action, int mods) {
    }

    public void characterEvent(char character) {
    }

    public void mouseButtonEvent(int button, boolean pressed, int x, int y) {
    }

    public void clickEvent(int button, int x, int y) {
    }

    public void cursorPosEvent(int x, int y) {
    }

    public boolean dragEvent(int button, float dx, float dy, float x, float y) {
        if (flyable) {
            GForm form = getForm();
            if (form != null) {
                GObject f = form.getFlyingObject();
                if (f == null) {
                    form.setFlyingObject(this);
                    flyOffsetX = (int) (x - getX());
                    flyOffsetY = (int) (y - getY());
                    doFlyBegin();
                } else if (f == this) {
                    doFlying();
                }
            }
        }
        return false;
    }

    public void dropEvent(int count, String[] paths) {
    }

    public void longTouchedEvent(int x, int y) {
    }

    public void keyEventGlfm(int key, int action, int mods) {
    }

    public void characterEvent(String str, int modifiers) {
    }

    public void touchEvent(int touchid, int phase, int x, int y) {
    }

    public boolean scrollEvent(float scrollX, float scrollY, float x, float y) {
        return false;
    }

    /**
     * 响应惯性事件,从P1到P2用了多长时间
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param moveTime
     */
    public boolean inertiaEvent(float x1, float y1, float x2, float y2, long moveTime) {
        return false;
    }

    public static boolean isInBoundle(float[] bound, float x, float y) {
        return x >= bound[LEFT] && x <= bound[LEFT] + bound[WIDTH] && y >= bound[TOP] && y <= bound[TOP] + bound[HEIGHT];
    }

    public boolean isInArea(float x, float y) {
        float absx = getX();
        float absy = getY();
        return x >= absx && x <= absx + getW() && y >= absy && y <= absy + getH();
    }

    public float[] getBoundle() {
        return boundle;
    }

    public <T extends GContainer> T getParent() {
        return (T) parent;
    }

    public void setParent(GContainer p) {
        parent = p;
    }

    public void setLocation(float x, float y) {
        boundle[LEFT] = x;
        boundle[TOP] = y;
        if (parent != null) {
            parent.reSize();
        }
    }

    public void setSize(float w, float h) {
        boundle[WIDTH] = w;
        boundle[HEIGHT] = h;
        if (parent != null) {
            parent.reSize();
        }
    }

    public float getLocationLeft() {
        return boundle[LEFT];
    }

    public float getLocationTop() {
        return boundle[TOP];
    }

    public float getX() {
        if (parent != null && !fixedLocation) {
            return parent.getInnerX() + boundle[LEFT];
        }
        return boundle[LEFT];
    }

    public float getY() {
        if (parent != null && !fixedLocation) {
            return parent.getInnerY() + boundle[TOP];
        }
        return boundle[TOP];
    }

    public float getW() {
        return boundle[WIDTH];
    }

    public float getH() {
        return boundle[HEIGHT];
    }

    public void move(float dx, float dy) {
        boundle[LEFT] += dx;
        boundle[TOP] += dy;
        if (parent != null) {
            parent.reSize();
        }
    }

    /**
     * @return the bgColor
     */
    public float[] getBgColor() {
        return bgColor;
    }

    /**
     * @param r
     * @param g
     * @param b
     * @param a
     */
    public void setBgColor(int r, int g, int b, int a) {
        bgColor = Nanovg.nvgRGBA((byte) r, (byte) g, (byte) b, (byte) a);
    }

    public void setBgColor(float[] color) {
        bgColor = color;
    }

    public void setBgColor(int rgba) {
        bgColor = nvgRGBA(rgba);
    }

    /**
     * @return the color
     */
    public float[] getColor() {
        return color;
    }

    /**
     * @param r
     * @param g
     * @param b
     * @param a
     */
    public void setColor(int r, int g, int b, int a) {
        color = Nanovg.nvgRGBA((byte) r, (byte) g, (byte) b, (byte) a);
        flyingColor = Nanovg.nvgRGBA((byte) r, (byte) g, (byte) b, (byte) (a / 2));
        a = a - 48;
        if (a < 0) a = 16;
        disabledColor = Nanovg.nvgRGBA((byte) r, (byte) g, (byte) b, (byte) a);
    }

    public void setColor(float[] color) {
        this.color = color;
        flyingColor = Nanovg.nvgRGBAf(color[0], color[1], color[2], color[3] * .5f);
        float a = color[3];
        a -= 0.125f;
        if (a < 0) a = .0625f;
        disabledColor = Nanovg.nvgRGBAf(color[0], color[1], color[2], a);
    }

    public void setColor(int rgba) {
        setColor(nvgRGBA(rgba));
    }

    public float getFontSize() {
        return fontSize;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * @return the actionListener
     */
    public GActionListener getActionListener() {
        return actionListener;
    }

    /**
     * @param actionListener the actionListener to set
     */
    public void setActionListener(GActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setVisible(boolean v) {
        visible = v;
    }

    public boolean isVisible() {
        return visible;
    }


    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }


    public GForm getForm() {
        return GCallBack.getInstance().getForm();
    }

    public GFrame getFrame() {
        GObject go = this;
        while (!(go instanceof GFrame)) {
            if (go == null) {
                return null;
            }
            go = go.getParent();
        }
        return (GFrame) go;
    }

    /**
     * @return the focusListener
     */
    public GFocusChangeListener getFocusListener() {
        return focusListener;
    }

    /**
     * @param focusListener the focusListener to set
     */
    public void setFocusListener(GFocusChangeListener focusListener) {
        this.focusListener = focusListener;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the attachment
     */
    public <T extends Object> T getAttachment() {
        return (T) attachment;
    }

    /**
     * @param attachment the attachment to set
     */
    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    /**
     * @return the front
     */
    public boolean isFront() {
        return front;
    }

    /**
     * @param front the front to set
     */
    public void setFront(boolean front) {
        this.front = front;
        if (front) this.back = false;
    }

    /**
     * @return the front
     */
    public boolean isBack() {
        return back;
    }

    /**
     * @param back the front to set
     */
    public void setBack(boolean back) {
        this.back = back;
        if (back) this.front = false;
    }

    public boolean isMenu() {
        return false;
    }

    public boolean isContextMenu() {
        return false;
    }

    void doAction() {
        if (actionListener != null && enable) {
            if (onClinkScript != null) {
                Interpreter inp = parseInpByCall(onClinkScript);
                String funcName = parseInstByCall(onClinkScript);
                if (inp != null && funcName != null) {
                    try {
                        inp.callSub(funcName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            actionListener.action(this);
        }
    }

    void doFocusLost(GObject newgo) {
        if (focusListener != null) {
            focusListener.focusLost(newgo);
        }
    }

    void doFocusGot(GObject oldgo) {
        if (focusListener != null) {
            focusListener.focusGot(oldgo);
        }
    }


    public GStateChangeListener getStateChangeListener() {
        return stateChangeListener;
    }

    public void setStateChangeListener(GStateChangeListener stateChangeListener) {
        this.stateChangeListener = stateChangeListener;
    }

    void doStateChanged(GObject go) {
        if (stateChangeListener != null) {
            if (onStateChangeScript != null) {
                Interpreter inp = parseInpByCall(onStateChangeScript);
                String funcName = parseInstByCall(onStateChangeScript);
                if (inp != null && funcName != null) {
                    try {
                        inp.callSub(funcName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            stateChangeListener.onStateChange(go);
        }
    }

    public String toString() {
        return super.toString() + "|" + name + "|" + text + "(" + boundle[LEFT] + "," + boundle[TOP] + "," + boundle[WIDTH] + "," + boundle[HEIGHT] + ")";
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public boolean isFlyable() {
        return flyable;
    }

    public void setFlyable(boolean flyable) {
        this.flyable = flyable;
    }

    public void setFlyListener(GFlyListener flyListener) {
        this.flyListener = flyListener;
    }

    public GFlyListener getFlyListener() {
        return flyListener;
    }

    void doFlyBegin() {
        if (flyListener != null && flyable) {
            flyListener.flyBegin(this, GCallBack.getInstance().getTouchOrMouseX(), GCallBack.getInstance().getTouchOrMouseY());
        }
    }

    void doFlyEnd() {
        if (flyListener != null && flyable) {
            flyListener.flyEnd(this, GCallBack.getInstance().getTouchOrMouseX(), GCallBack.getInstance().getTouchOrMouseY());
        }
    }

    void doFlying() {
        if (flyListener != null && flyable) {
            flyListener.flying(this, GCallBack.getInstance().getTouchOrMouseX(), GCallBack.getInstance().getTouchOrMouseY());
        }
    }

    public boolean isFlying() {
        return getForm() != null && getForm().getFlyingObject() == this;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getCmd() {
        return this.cmd;
    }


    //================================  script   =====================================

    /**
     * @param funcCall
     * @return
     */
    protected Interpreter parseInpByCall(String funcCall) {
        Interpreter inp = null;
        //two call formate:
        // framename.fun(1,2)
        // fun(1,2)
        int leftQ = funcCall.indexOf('(');
        int point = funcCall.indexOf('.');
        if (leftQ > 0) {
            if (point >= 0 && point < leftQ) {
                String containerName = funcCall.substring(0, point);
                inp = getInterpreter(containerName);
            } else {
                inp = getInterpreter();
            }
        }
        return inp;
    }

    protected String parseInstByCall(String funcCall) {
        String funcName = null;
        //two call formate:
        // framename.fun(1,2)
        // fun(1,2)
        int leftQ = funcCall.indexOf('(');
        int point = funcCall.indexOf('.');
        if (leftQ > 0) {
            if (point >= 0 && point < leftQ) {
                funcName = funcCall.substring(point + 1);
            } else {
                funcName = funcCall;
            }
        }
        return funcName;
    }


    public void setOnClinkScript(String onClinkScript) {
        this.onClinkScript = onClinkScript;
    }

    public void setOnStateChangeScript(String onStateChangeScript) {
        this.onStateChangeScript = onStateChangeScript;
    }

    public String getOnClinkScript() {
        return onClinkScript;
    }

    public String getOnStateChangeScript() {
        return onStateChangeScript;
    }

    public Interpreter getInterpreter() {
        return getInterpreter(null);
    }

    public Interpreter getInterpreter(String containerName) {
        if (parent == null) return null;
        if (containerName == null) return parent.getInterpreter();
        return parent.getInterpreter(containerName);
    }
    //================================    =====================================


    public GLayout getLayout() {
        return layout;
    }

    public void setLayout(GLayout layout) {
        this.layout = layout;
    }

}
