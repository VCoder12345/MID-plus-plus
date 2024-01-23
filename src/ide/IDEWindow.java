package ide;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;

public class IDEWindow extends JFrame {
	public IDEWindow() {
		setSize(1800, 1000);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		Container cp = getContentPane();
		JTextPane pane = new JTextPane();
		SimpleAttributeSet attributeSet = new SimpleAttributeSet();
		
		pane.setCharacterAttributes(attributeSet, true);
		pane.setText("hello");
		pane.setFont(new Font("Arial", Font.PLAIN, 18));
		
		JScrollPane scrollPane = new JScrollPane(pane);
		cp.add(scrollPane, BorderLayout.CENTER);
		
		setVisible(true);
	}
	
	public static void main(String[] args) {
		new IDEWindow();
	}
}
