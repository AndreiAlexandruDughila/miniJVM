package org.mini.xmlui;

import org.mini.gui.GImage;
import org.mini.gui.GList;
import org.mini.gui.GListItem;
import org.mini.gui.GObject;
import org.mini.gui.event.GActionListener;
import org.mini.xmlui.xmlpull.KXmlParser;
import org.mini.xmlui.xmlpull.XmlPullParser;

import java.util.Vector;

public class XList extends XObject implements GActionListener {
    static public final String XML_NAME = "list";

    static class ListItem {
        static public final String XML_NAME = "li";
        String name;
        String text;
        String pic;
    }

    Vector items = new Vector();

    GList list;
    boolean multiLine = false;
    int itemheight = XDef.DEFAULT_LIST_HEIGHT;

    public XList(XContainer xc) {
        super(xc);
    }

    @Override
    String getXmlTag() {
        return XML_NAME;
    }

    void parseMoreAttribute(String attName, String attValue) {
        super.parseMoreAttribute(attName, attValue);
        if (attName.equals("multiline")) {
            multiLine = "0".equals(attValue) ? false : true;
        } else if (attName.equals("itemh")) {
            itemheight = Integer.parseInt(attValue);
        }
    }


    /**
     * 解析
     *
     * @param parser KXmlParser
     * @throws Exception
     */
    public void parse(KXmlParser parser) throws Exception {
        super.parse(parser);
        int depth = parser.getDepth();

        //得到域
        do {
            parser.next();
            String tagName = parser.getName();
            if (parser.getEventType() == XmlPullParser.START_TAG) {

                if (tagName.equals(XList.ListItem.XML_NAME)) {
                    XList.ListItem item = new ListItem();

                    item.name = parser.getAttributeValue(null, "name");
                    item.pic = parser.getAttributeValue(null, "pic");
                    String tmp = parser.nextText();
                    item.text = tmp == null ? "" : tmp;
                    items.add(item);
                }
                toEndTag(parser, XList.ListItem.XML_NAME);
                parser.require(XmlPullParser.END_TAG, null, tagName);
            }
        }
        while (!(parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equals(XML_NAME) && depth == parser.getDepth()));

    }

    void preAlignVertical() {
        if (height == XDef.NODEF) {
            int parentTrialViewH = parent.getTrialViewH();
            if (raw_heightPercent != XDef.NODEF && parentTrialViewH != XDef.NODEF) {
                viewH = height = raw_heightPercent * parentTrialViewH / 100;
            } else {
                viewH = height = XDef.DEFAULT_LIST_HEIGHT;
            }
        }
    }

    void preAlignHorizontal() {
        if (width == XDef.NODEF) {
            if (raw_widthPercent == XDef.NODEF) {
                viewW = width = parent.viewW;
            } else {
                viewW = width = raw_widthPercent * parent.viewW / 100;
            }
        }
    }

    public GObject getGui() {
        return list;
    }

    void createGui() {
        if (list == null) {
            list = new GList(x, y, width, height);
            list.setShowMode(multiLine ? GList.MODE_MULTI_SHOW : GList.MODE_SINGLE_SHOW);
            list.setName(name);
            list.setItemHeight(itemheight);
            list.setAttachment(this);
            for (int i = 0; i < items.size(); i++) {
                ListItem item = (ListItem) items.elementAt(i);
                GImage img = null;
                if (item.pic != null) {
                    img = GImage.createImageFromJar(item.pic);
                }
                GListItem gli = new GListItem(img, item.text);
                gli.setName(item.name);
                gli.setActionListener(this);

                list.addItem(gli);
            }
        } else {
            list.setLocation(x, y);
            list.setSize(width, height);
        }
    }


    @Override
    public void action(GObject gobj) {
        getRoot().getEventHandler().action(gobj, null);
    }

}