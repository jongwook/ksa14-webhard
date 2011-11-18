package org.ksa14.webhard.ui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.ksa14.webhard.MsgListener;

public class StatusBarLabel extends JLabel implements MsgListener {
	private static final long serialVersionUID = 0L;
	
	public StatusBarLabel(String value) {
		super(value);
		// Try to set system native look-and-feel
		SwingUtility.setSystemLookAndFeel();
		
		setBorder(BorderFactory.createEtchedBorder());
	}

	@Override
	public void ReceiveMsg(final int type, final Object arg) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (type == MsgListener.STATUS_INFO)
					setText(arg.toString());
			}
		});		
	}
}
