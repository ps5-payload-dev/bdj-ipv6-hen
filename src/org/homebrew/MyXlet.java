package org.homebrew;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import javax.tv.xlet.Xlet;
import javax.tv.xlet.XletContext;
import org.havi.ui.HScene;
import org.havi.ui.HSceneFactory;
import org.havi.ui.event.HRcEvent;
import org.dvb.event.EventManager;
import org.dvb.event.UserEvent;
import org.dvb.event.UserEventListener;
import org.dvb.event.UserEventRepository;
import javax.net.ssl.*;

public class MyXlet implements UserEventListener, Xlet {
    private static String ELFLDR_URL  = "https://github.com/ps5-payload-dev/elfldr/releases/latest/download/Payload.zip";
    private static String KLOGSRV_URL = "https://github.com/ps5-payload-dev/klogsrv/releases/latest/download/Payload.zip";
    private static String FTPSRV_URL  = "https://github.com/ps5-payload-dev/ftpsrv/releases/latest/download/Payload.zip";
    private static String WEBSRV_URL  = "https://github.com/ps5-payload-dev/websrv/releases/latest/download/Payload.zip";
    private static String SHSRV_URL   = "https://github.com/ps5-payload-dev/shsrv/releases/latest/download/Payload.zip";
    private static String GDBSRV_URL  = "https://github.com/ps5-payload-dev/gdbsrv/releases/latest/download/Payload.zip";

    private HScene scene;
    private LoggingUI logUI;
    private ListUI listUI;
    private UserEventRepository evtRepo;

    private void addPayload(String title, URL url) {
	listUI.addItem(title,
		       new Runnable() {
			   public void run() {
			       try {
				   libkernel.sendNotificationRequest("Launching " + url.toString());
				   HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
				   SSLContext ctx = SSLContext.getInstance("TLSv1.2");

				   ctx.init(null, null, null);
				   conn.setSSLSocketFactory(ctx.getSocketFactory());

				   ZipInputStream is = new ZipInputStream(conn.getInputStream());
				   ZipEntry entry = is.getNextEntry();

				   ElfLoading.runElf("localhost", 9021, is);
			       } catch (Throwable t) {
				   libkernel.sendNotificationRequest(t.getMessage());
				   LoggingUI.getInstance().log(t);
				   logUI.setVisible(true);
			       }
			   }
		       });
    }

    private void addPayload(String title, File file) {
	listUI.addItem(title,
		       new Runnable() {
			   public void run() {
			       try {
				   libkernel.sendNotificationRequest("Launching " + file.toString());
				   ElfLoading.runElf("localhost", 9021, new FileInputStream(file));
			       } catch (Throwable t) {
				   libkernel.sendNotificationRequest(t.getMessage());
				   LoggingUI.getInstance().log(t);
				   logUI.setVisible(true);
			       }
			   }
		       });

    }
    
    public void initXlet(XletContext context) {
	logUI = LoggingUI.getInstance();
	logUI.setSize(1280, 720);

	listUI = new ListUI();
	listUI.setSize(1280, 720);

	// remote ELFs
	try {
	    listUI.addItem("Launch payloads from https://github.com/ps5-payload-dev");
	    addPayload("klogsrv.elf - A kernel logging server running on port 3232",new URL(KLOGSRV_URL));
	    addPayload("ftpsrv.elf  - An FTP server running on prt 2121", new URL(FTPSRV_URL));
	    addPayload("websrv.elf  - A web server running on port 8080", new URL(WEBSRV_URL));
	    addPayload("shsrv.elf   - A Telnet server running on port 2323", new URL(SHSRV_URL));
	    addPayload("gdbsrv.elf  - A GDB server running on port 2159", new URL(GDBSRV_URL));
	    addPayload("elfldr.elf  - An ELF loader running on port 9021", new URL(ELFLDR_URL));
	} catch (Throwable t) {
	    LoggingUI.getInstance().log(t);
	}

	// local ELFs
	listUI.addItem("Launch payloads from disc");
	addPayload("klogsrv.elf - A kernel logging server running on port 3232", new File("/disc/klogsrv.elf"));
	addPayload("ftpsrv.elf  - An FTP server running on prt 2121", new File("/disc/ftpsrv.elf"));
	addPayload("websrv.elf  - A web server running on port 8080", new File("/disc/websrv.elf"));
	addPayload("shsrv.elf   - A Telnet server running on port 2323", new File("/disc/shsrv.elf"));
	addPayload("gdbsrv.elf  - A GDB server running on port 2159", new File("/disc/gdbsrv.elf"));

	listUI.setSelected(1);

	evtRepo = new UserEventRepository("input");
	evtRepo.addKey(HRcEvent.VK_ENTER);
	evtRepo.addKey(HRcEvent.VK_UP);
	evtRepo.addKey(HRcEvent.VK_DOWN);
	evtRepo.addKey(461); // □

	scene = HSceneFactory.getInstance().getDefaultHScene();
	scene.add(logUI, BorderLayout.CENTER);
	scene.add(listUI, BorderLayout.CENTER);
        scene.validate();
	scene.repaint();
    }

    public void startXlet() {
        scene.setVisible(true);

	try {
	    LoggingUI.getInstance().log("[*] Disabling Java security manager...");
	    PrivilegeEscalation.disableSecurityManager();
	    LoggingUI.getInstance().log("[+] Java security manager disabled");

	    LoggingUI.getInstance().log("[*] Obtaining kernel .data R/W capabilities...");
	    KernelMemory.enableRW();
	    LoggingUI.getInstance().log("[+] Kernel .data R/W achieved");
	     
	    KernelPatching.escalatePrivileges();
	    KernelPatching.setSceAuthId(0x4801000000000013l);
	    KernelPatching.setSceCaps(0xffffffffffffffffl, 0xffffffffffffffffl);
	    KernelPatching.setSceAttr(KernelPatching.getSceAttr() | 0x80);
	    LoggingUI.getInstance().log("[+] Escalated privileges");

	    KernelPatching.setSecurityFlags(KernelPatching.getSecurityFlags() | 0x14);
	    KernelPatching.setUtokenFlags((byte)(KernelPatching.getUtokenFlags() | 0x1));
	    KernelPatching.setQAFlags(KernelPatching.getQAFlags() | 0x0000000000010300l);
	    KernelPatching.setTargetId((byte)0x82);
	    LoggingUI.getInstance().log("[+] Debug/dev mode enabled");

	    ElfLoading.runElf("/disc/elfldr.elf");

	    logUI.setVisible(false);
	    EventManager.getInstance().addUserEventListener(this, evtRepo);
	} catch (Throwable t) {
	    LoggingUI.getInstance().log(t);
	}
    }

    public void pauseXlet() {
	scene.setVisible(false);
    }

    public void destroyXlet(boolean unconditional) {
        scene = null;
    }

    public void userEventReceived(UserEvent evt) {
	if(evt.getType() != KeyEvent.KEY_RELEASED) {
	    return;
	}

	if(evt.getCode() == 461) {
	    logUI.setVisible(!logUI.isVisible());
	    return;
	}

	switch(evt.getCode()) {
	case HRcEvent.VK_ENTER:
	    Runnable r = (Runnable)listUI.getSelected();
	    if(r != null) {
		r.run();
	    }
	    break;
	case HRcEvent.VK_UP:
	    listUI.itemUp();
	    break;
	case HRcEvent.VK_DOWN:
	    listUI.itemDown();
	    break;
	}
    }
}
