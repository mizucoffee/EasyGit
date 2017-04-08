package net.mizucoffee.easygit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.AbortedByHookException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Main {
	static Shell shell;
	static Label folderLabel;
	static Git git = null;
	static Button acpBtn;
	static Text text;
	
	static String key = "SDskjrdg";
	
	static String id;
	static String pw;
	
	public static void main(String[] args) {
		Display display = new Display ();
		shell = new Shell (display);
		shell.setText("EasyGit");
		
		folderLabel = new Label(shell, SWT.NONE);
		folderLabel.setText("フォルダ:");
		
		Label label = new Label(shell, SWT.NONE);
		label.setText("作業内容");
		
		
		text = new Text(shell, SWT.BORDER);
		text.setEnabled(false);

		Button selectBtn = new Button(shell, SWT.PUSH);
		selectBtn.setText("フォルダ選択");
		selectBtn.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog directoryDialog = new DirectoryDialog(shell,SWT.OPEN);
				directoryDialog.setMessage("プロジェクトのフォルダを指定してください。");
				String path = directoryDialog.open();
				if(path == null) return;
				try {
					git = Git.open(new File(path));
				} catch (RepositoryNotFoundException e1) {
					InputDialog dialog = new InputDialog(shell);
					dialog.setText("確認");
					dialog.setMessage("このディレクトリはGitで管理されていません。Git管理下にしますか？\n" + path);
					if(dialog.open() == 0){
						try {
							git = Git.init().setDirectory(new File(path)).call();
							
							StoredConfig config = git.getRepository().getConfig();
							config.setString("remote", "origin", "url", dialog.getValue() + ".git");
							config.setString("remote", "origin", "fetch", "+refs/heads/*:refs/remotes/origin/*");
							config.save();
							
						} catch (IllegalStateException e2) {
							e2.printStackTrace();
							return;
						} catch (GitAPIException e2) {
							e2.printStackTrace();
							return;
						} catch (IOException e2) {
							e2.printStackTrace();
							return;
						}
					}else {
						return;
					}
				} catch (IOException e1) {
					e1.printStackTrace();
					return;
				}
				folderLabel.setText("フォルダ:" + git.getRepository().getWorkTree().getPath());
				text.setEnabled(true);
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			
		});
		
		acpBtn = new Button(shell, SWT.PUSH);
		acpBtn.setText("ACP(転送)");
		acpBtn.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				try {
					if(git.status().call().isClean()){
						MessageBox box = new MessageBox(shell);
						box.setMessage("最新の状態です！");
						box.setText("ACP");
						box.open();
						return;
					}
					git.add().addFilepattern(".").call();
					git.commit().setAll(true).setMessage(text.getText()).call();
					CredentialsProvider cp = new UsernamePasswordCredentialsProvider( id,pw );
					git.push().setCredentialsProvider(cp).setRemote("origin").call();
					MessageBox box = new MessageBox(shell);
					box.setText("ACP完了");
					box.setMessage("ACPが完了しました！");
					box.open();
				} catch (NoHeadException e2) {
					e2.printStackTrace();
				} catch (NoMessageException e2) {
					e2.printStackTrace();
				} catch (UnmergedPathsException e2) {
					e2.printStackTrace();
				} catch (ConcurrentRefUpdateException e2) {
					e2.printStackTrace();
				} catch (WrongRepositoryStateException e2) {
					e2.printStackTrace();
				} catch (AbortedByHookException e2) {
					e2.printStackTrace();
				} catch (GitAPIException e2) {
					e2.printStackTrace();
				}
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		acpBtn.setEnabled(false);
		
		text.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event arg0) {
				if(text.getText().equals(""))
					acpBtn.setEnabled(false);
				else
					acpBtn.setEnabled(true);
			}
		});
		
		final int inset = 8;
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth = inset;
		formLayout.marginHeight = inset;
		shell.setLayout (formLayout);
		
		FormData selectBtnData = new FormData (100,SWT.DEFAULT);
		selectBtnData.top = new FormAttachment(0,0);
		selectBtnData.right = new FormAttachment (100, 0);
		selectBtn.setLayoutData (selectBtnData);
		
		FormData folderData = new FormData (300,SWT.DEFAULT);
		folderData.left = new FormAttachment(0,4);
		folderData.top = new FormAttachment(0,4);
		folderData.right = new FormAttachment (selectBtn, -inset);
		folderLabel.setLayoutData (folderData);
		
		FormData labelData = new FormData (300,SWT.DEFAULT);
		labelData.left = new FormAttachment(0,4);
		labelData.top = new FormAttachment(selectBtn,0);
		label.setLayoutData (labelData);
		
		
		FormData acpData = new FormData (100,SWT.DEFAULT);
		acpData.top = new FormAttachment(label,4);
		acpData.right = new FormAttachment (100, 0);
		acpBtn.setLayoutData (acpData);
		
		
		
		FormData textData = new FormData (300,SWT.DEFAULT);
		textData.left = new FormAttachment(0,4);
		textData.top = new FormAttachment(label,6);
		textData.right = new FormAttachment (acpBtn, -4);
		text.setLayoutData (textData);
		
		shell.pack ();
		shell.open ();
		
		check();
		
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		
		display.dispose ();
	}
	
	private static void check() {
		if(!new File("config.dat").exists()){
			LoginInputDialog loginInputDialog = new LoginInputDialog(shell);
			loginInputDialog.setText("初期設定");
			loginInputDialog.setMessage("Githubのログイン情報を入力してください。\nログイン情報を変更する場合はconfig.datを削除してください。");
			if(loginInputDialog.open() == 0){
				if(loginInputDialog.getId().equals("") || loginInputDialog.getPw().equals("")){
					MessageBox box = new MessageBox(shell);
					box.setText("エラー");
					box.setMessage("入力に不備があります。もう一度入力してください。");
					box.open();
					check();
				}
				GitHubClient gitHubClient = new GitHubClient();
				gitHubClient.setCredentials(loginInputDialog.getId(), loginInputDialog.getPw());
				try {
					gitHubClient.get(new GitHubRequest().setUri(""));
					
				} catch (IOException e2) {
					e2.printStackTrace();
					MessageBox box = new MessageBox(shell);
					box.setText("エラー");
					box.setMessage("ログインに失敗しました。もう一度お試しください。");
					box.open();
					check();
				}
				
				String text = loginInputDialog.getId() + "," + loginInputDialog.getPw();
				try {
					FileOutputStream fileOutStm = null;
					try {
						fileOutStm = new FileOutputStream("config.dat");
						fileOutStm.write(encrypt(key, text));
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}finally {
						try {
							fileOutStm.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} catch (InvalidKeyException e1) {
					e1.printStackTrace();
				} catch (IllegalBlockSizeException e1) {
					e1.printStackTrace();
				} catch (NoSuchAlgorithmException e1) {
					e1.printStackTrace();
				} catch (BadPaddingException e1) {
					e1.printStackTrace();
				} catch (NoSuchPaddingException e1) {
					e1.printStackTrace();
				}
			}
		}else {
			final File file = new File("config.dat");
			final long fileSize = file.length();
			final int byteSize = (int) fileSize;
			byte[] bytes = new byte[byteSize];
			try {
			    RandomAccessFile raf = new RandomAccessFile(file, "r");
			    try {
			        raf.readFully(bytes);
			    } finally {
			        raf.close();
			    }
			} catch (IOException e) {
			    e.printStackTrace();
			}
			try {
				String s = decrypt(key, bytes);
				String[] data = s.split(",");
				id = data[0];
				pw = data[1];
			} catch (InvalidKeyException e1) {
				e1.printStackTrace();
			} catch (IllegalBlockSizeException e1) {
				e1.printStackTrace();
			} catch (NoSuchAlgorithmException e1) {
				e1.printStackTrace();
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			} catch (BadPaddingException e1) {
				e1.printStackTrace();
			} catch (NoSuchPaddingException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	private static byte[]   encrypt(String key, String text)
	        throws javax.crypto.IllegalBlockSizeException,
	            java.security.InvalidKeyException,
	            java.security.NoSuchAlgorithmException,
	            java.io.UnsupportedEncodingException,
	            javax.crypto.BadPaddingException,
	            javax.crypto.NoSuchPaddingException
	{
	    javax.crypto.spec.SecretKeySpec sksSpec = 
	        new javax.crypto.spec.SecretKeySpec(key.getBytes(), "Blowfish");
	    javax.crypto.Cipher cipher = 
	        javax.crypto.Cipher.getInstance("Blowfish");
	    cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, sksSpec);
	    byte[] encrypted = cipher.doFinal(text.getBytes());
	    return encrypted;
	}
	
	private static String   decrypt(String key, byte[] encrypted)
	        throws javax.crypto.IllegalBlockSizeException,
	            java.security.InvalidKeyException,
	            java.security.NoSuchAlgorithmException,
	            java.io.UnsupportedEncodingException,
	            javax.crypto.BadPaddingException,
	            javax.crypto.NoSuchPaddingException
	{
	    javax.crypto.spec.SecretKeySpec sksSpec = 
	        new javax.crypto.spec.SecretKeySpec(key.getBytes(), "Blowfish");
	    javax.crypto.Cipher cipher = 
	        javax.crypto.Cipher.getInstance("Blowfish");
	    cipher.init(javax.crypto.Cipher.DECRYPT_MODE, sksSpec);
	    byte[] decrypted = cipher.doFinal(encrypted);
	    return new String(decrypted);
	}
}