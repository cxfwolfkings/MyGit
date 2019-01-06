package com.colin.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.colin.entity.Product;

/**
 * 
 * @author  Colin Chen
 * @create  2018年12月1日 上午5:22:08
 * @modify  2018年12月1日 上午5:22:08
 * @version A.1
 */
public class SelectFrame extends JFrame {

	private static final long serialVersionUID = 2158435747845178551L;
	private ClientContext clientContext;

	public void setClientContext(ClientContext clientContext) {
		this.clientContext = clientContext;
	}

	public SelectFrame() {
		init();
	}

	private void init() {
		setTitle("Products");
		setSize(460, 300);
		setLocationRelativeTo(null);
		setContentPane(createContentPane());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				clientContext.exit(SelectFrame.this);
			}
		});
	}

	private JPanel createContentPane() {
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(new EmptyBorder(10, 10, 10, 10));
		p.add(BorderLayout.NORTH, new JLabel("Hello", JLabel.CENTER));
		p.add(BorderLayout.CENTER, createCenterPane());
		p.add(BorderLayout.SOUTH, createBtnPane());
		return p;
	}

	private JPanel createCenterPane() {
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(new EmptyBorder(8, 8, 8, 8));
		p.add(BorderLayout.NORTH, createIdPwdPane());
		message = new JLabel("This is a message", JLabel.CENTER);
		p.add(BorderLayout.CENTER, message);
		return p;
	}

	private JPanel createIdPwdPane() {
		JPanel p = new JPanel(new GridLayout(8, 4, 4, 6));
		p.add(createIdPane());
		p.add(createPwdPane());
		p.add(createRsPane());
		return p;
	}

	private JPanel createIdPane() {
		JPanel p = new JPanel(new BorderLayout(6, 0));
		// p.setLayout();
		p.add(BorderLayout.WEST, new JLabel("UserName: "));
		idField1 = new JTextField();
		p.add(BorderLayout.CENTER, idField1);
		return p;
	}

	private JPanel createPwdPane() {
		JPanel p = new JPanel(new BorderLayout(6, 0));
		p.add(BorderLayout.WEST, new JLabel("Password: "));
		idField2 = new JTextField();
		p.add(BorderLayout.CENTER, idField2);
		return p;
	}

	private JScrollPane createRsPane() {
		JScrollPane pane = new JScrollPane();
		pane.setBorder(new TitledBorder("Content"));
		rsArea = new JTextArea();
		rsArea.setLineWrap(true);
		rsArea.setEditable(false);
		pane.getViewport().add(rsArea);
		return pane;
	}

	private JPanel createBtnPane() {
		JPanel p = new JPanel(new FlowLayout());
		JButton select = new JButton("Select");
		JButton cancel = new JButton("Cancel");
		p.add(select);
		p.add(cancel);
		getRootPane().setDefaultButton(select);
		select.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				clientContext.select();
			}
		});
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				clientContext.exit(SelectFrame.this);
			}
		});
		return p;
	}

	private JLabel message;
	private JTextField idField1;
	private JTextField idField2;
	private JTextArea rsArea;

	public static void main(String[] args) {
		new SelectFrame().setVisible(true);
	}

	public String getProductname() {
		// TODO Auto-generated method stub
		return new String(idField1.getText());
	}

	public String getProducttype() {
		// TODO Auto-generated method stub
		return new String(idField2.getText());
	}

	// public void updaterArea() {
	// // TODO Auto-generated method stub
	//
	// }

	public void setRsarea(List<Product> list) {
		// TODO Auto-generated method stub
		String str = "";
		for (Product p : list) {
			str += "name:" + p.getName() + "--type:" + p.getType() + "--cost:"
					+ p.getCost() + "\n";
		}
		rsArea.setText(str);
	}
}
