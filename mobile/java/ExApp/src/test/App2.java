/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.io.File;
import org.mini.glfm.Glfm;
import org.mini.gui.GApplication;
import org.mini.gui.GForm;
import org.mini.gui.GFrame;
import org.mini.gui.GGraphics;
import org.mini.gui.GImage;
import org.mini.gui.GList;
import org.mini.gui.GMenu;
import org.mini.gui.GMenuItem;
import org.mini.gui.GObject;
import org.mini.gui.GuiCallBack;
import org.mini.gui.event.GActionListener;

/**
 *
 * @author Gust
 */
public class App2 implements GApplication {

    private static App2 app;

    GForm form;
    GMenu menu;
    int menuH = 80;

    static public App2 getInstance() {
        if (app == null) {
            app = new App2();
        }
        return app;
    }

    @Override
    public GForm createdForm(final GuiCallBack ccb) {
        if (form != null) {
            return form;
        }

        form = new GForm(/*"GuiTest"*/"登录 窗口", 800, 600, ccb);

        form.setFps(30f);
        long vg = form.getNvContext();
        GFrame gframe = new GFrame("第二个应用", 0, 0, ccb.getDeviceWidth(), ccb.getDeviceHeight() - menuH);
        form.add(gframe);
        gframe.align(GGraphics.TOP | GGraphics.HCENTER);

        GList list = new GList(0, 0, (int) gframe.getView().getW(), (int) (gframe.getView().getH() - 40));
        //list.setMode(GList.MODE_MULTI_LINE);
        gframe.add(list);

        String resRoot = Glfm.glfmGetResRoot();
        File f = new File(resRoot);
        if (f.exists()) {
            String[] files = f.list();
            GImage[] imgs = new GImage[files.length];
            list.setItems(imgs, files);
        }
        list.setActionListener(new GActionListener() {
            @Override
            public void action(GObject gobj) {
                GList glist = (GList) gobj;
                System.out.println(glist.getSelectedIndex());
            }
        });

        GImage img = new GImage("./image4.png");
        menu = new GMenu(0, form.getDeviceHeight() - menuH, form.getDeviceWidth(), menuH);
        GMenuItem item = menu.addItem("退出", img);
        item.setActionListener(new GActionListener() {
            @Override
            public void action(GObject gobj) {
                ccb.setApplication(App1.getInstance());
            }
        });
        menu.addItem("主页", img);
        menu.addItem("搜索", img);
        menu.addItem("发现", img);
        menu.addItem("我的", img);
        form.add(menu);

        return form;
    }
}
