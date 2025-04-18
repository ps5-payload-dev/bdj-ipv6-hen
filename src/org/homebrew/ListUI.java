package org.homebrew;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;

public class ListUI extends Container {
    private final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 22);
    private final Color fontColor = new Color(240, 240, 240);
    private final Color disabledColor = new Color(140, 140, 140);
    private final Color selectedColor = new Color(240, 240, 0);
    private final Color bgColor = new Color(5, 5, 5);
    private final ArrayList labels = new ArrayList();
    private final ArrayList items = new ArrayList();
    private final int VISIBLE_ITEMS = 26;

    private int topItem = 0;
    private int selectedItem = -1;
    private int bottomItem = VISIBLE_ITEMS;
    private int activeItem = -1;

    void addItem(String label, Runnable item) {
	items.add(item);
	labels.add(label);

	if(bottomItem - topItem < VISIBLE_ITEMS) {
	    bottomItem += 1;
	}

	if(selectedItem == -1 && item != null) {
	    selectedItem = items.size() - 1;
	}
	repaint();
    }

    void addItem(String label) {
	addItem(label, null);
    }

    Runnable getSelected() {
	return (Runnable)items.get(selectedItem);
    }

    void setSelected(int index) {
	if(index >= 0 && index < items.size()) {
	    selectedItem = index;
	}
    }

    void itemUp() {
	int moves = 0;
	for(int i=selectedItem-1; i>0; i--) {
	    moves += 1;
	    if(items.get(i) != null) {
		break;
	    }
	}

	if(moves == 0) {
	    return;
	}

	selectedItem -= moves;
	if(selectedItem < topItem) {
	    topItem -= moves;
	    bottomItem -= moves;
	}

	repaint();
    }

    void itemDown() {
	int moves = 0;

	for(int i=selectedItem+1; i<items.size(); i++) {
	    moves += 1;
	    if(items.get(i) != null) {
		break;
	    }
	}

	if(moves == 0) {
	    return;
	}

	selectedItem += moves;
	if(selectedItem > bottomItem) {
	    topItem += moves;
	    bottomItem += moves;
	}

	repaint();
    }

    void itemActivate() {
	Runnable r =  (Runnable)items.get(selectedItem);
	if (r == null) {
	    return;
	}
        Thread trd = new Thread(r);

	activeItem = items.indexOf(r);
        trd.start();

        try {
            while (trd.isAlive()) {
                repaint();
            }

            trd.join();
        } catch (Throwable t) {
        }

	activeItem = -1;
    }

    public void paint(Graphics g) {
	g.setFont(font);
	g.setColor(bgColor);
	g.fillRect(20, 20, getWidth() - 40, getHeight() - 40);
	g.setColor(fontColor);

	for(int i=topItem; i<=bottomItem && i<items.size(); i++) {
	    String label = (String)labels.get(i);
	    String prefix = "";

	    if(items.get(i) == null) {
		g.setColor(disabledColor);
	    } else if(i == selectedItem) {
		g.setColor(selectedColor);
	    } else {
		g.setColor(fontColor);
	    }

	    if(i == activeItem) {
		long ms = System.currentTimeMillis();
		switch((int)(ms/100) % 4) {
		case 0:
		    prefix = "/  ";
		    break;
		case 1:
		    prefix = "-  ";
		    break;
		case 2:
		    prefix = "\\  ";
		    break;
		case 3:
		    prefix = "|  ";
		    break;
		}
	    } else if(i == selectedItem) {
		prefix = "-> ";
	    }  else if(items.get(i) != null) {
		prefix = "   ";
	    }
	    g.drawString(prefix+label, 30, 40 + ((i-topItem)*25));
	}
    }
}
