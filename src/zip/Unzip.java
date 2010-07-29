package zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Unzip {
	private static final int BUFFER = 1024;

	public static File unzip(String zipFileName, String outfolder) {
		File inFile = new File(zipFileName);
		if (!inFile.exists())
			return null;
		/*
		 * if(outfolder.endsWith("/") || outfolder.endsWith("\\")) outfolder =
		 * outfolder.substring(0, outfolder.length()-1); outfolder +=
		 * "/"+inFile.getName().substring(0, inFile.getName().lastIndexOf("."));
		 */
		File outFolder = new File(outfolder);
		if (!outFolder.exists())
			outFolder.mkdirs();
		System.out.println("Unzipping : " + zipFileName);
		try {
			BufferedOutputStream out = null;
			ZipInputStream in = new ZipInputStream(new BufferedInputStream(
					new FileInputStream(inFile)));
			ZipEntry entry;
			while ((entry = in.getNextEntry()) != null) {
				// System.out.println("Extracting: " + entry);
				int count;
				byte data[] = new byte[BUFFER];

				// create directories
				int pos = entry.getName().lastIndexOf('\\'); // for windows
				if (pos == -1)
					pos = entry.getName().lastIndexOf('/'); // for unix
				if (pos != -1) {
					String dirs = entry.getName().substring(0, pos);

					File F = new File(outFolder.getAbsolutePath() + "/" + dirs);
					F.mkdirs();
				}

				// write the files to the disk
				if (entry.isDirectory())
					new File(outfolder + "/" + entry.getName()).mkdirs();
				else {

					out = new BufferedOutputStream(
							new FileOutputStream(outFolder.getAbsolutePath()
									+ "/" + entry.getName()), BUFFER);

					while ((count = in.read(data, 0, BUFFER)) != -1) {
						out.write(data, 0, count);
					}
					cleanUp(out);
				}
			}
			cleanUp(in);
			return outFolder;
		} catch (Exception e) {
			e.printStackTrace();
			return inFile;
		}
	}

	private static void cleanUp(InputStream in) throws Exception {
		in.close();
	}

	private static void cleanUp(OutputStream out) throws Exception {
		out.flush();
		out.close();
	}

	@SuppressWarnings("unchecked")
	public static String[] listEntries(String infile) {
		ZipFile zipFile;
		Vector<String> Entries = new Vector<String>();

		try {
			zipFile = new ZipFile(infile);

			Enumeration zipEntries = zipFile.entries();

			while (zipEntries.hasMoreElements()) {
				Entries.add(((ZipEntry) zipEntries.nextElement()).getName());
				// System.out.println(Entries.lastElement());
			}

			Object[] obj = Entries.toArray();
			String[] list = new String[obj.length];
			for (int i = 0; i < obj.length; i++)
				list[i] = (String) obj[i];

			return list;
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static void unzipFile(String zipFileName, String outfolder,
			String fileName) {
		File inFile = new File(zipFileName);
		if (!inFile.exists())
			return;
		File outFolder = new File(outfolder);
		if (!outFolder.exists())
			outFolder.mkdirs();
		System.out.println("Unzipping : " + fileName + " from " + zipFileName);
		ZipFile zip;
		try {
			zip = new ZipFile(new File(zipFileName));

			for (Enumeration e = zip.entries(); e.hasMoreElements();) {
				ZipEntry entry = (ZipEntry) e.nextElement();
				if (entry.getName().indexOf(fileName) == -1)
					continue;
				// System.out.println("File name: " + entry.getName() +
				// "; size: " + entry.getSize()
				// + "; compressed size: " + entry.getCompressedSize());
				InputStream is = zip.getInputStream(entry);
				InputStreamReader in = new InputStreamReader(is);
				BufferedOutputStream out = new BufferedOutputStream(
						new FileOutputStream(outFolder.getAbsolutePath() + "/"
								+ fileName), BUFFER);

				int count;
				char[] buffer = new char[BUFFER];
				while ((count = in.read(buffer, 0, buffer.length)) != -1) {
					String s = new String(buffer);
					out.write(s.getBytes(), 0, count);
				}

				cleanUp(out);
				in.close();
			}
		} catch (ZipException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void extractFromJar(String jarFileName, String outfolder,
			String fileName){
		File inFile = new File(jarFileName);
		if (!inFile.exists())
			return;
		File outFolder = new File(outfolder);
		if (!outFolder.exists())
			outFolder.mkdirs();
		System.out.println("Extracting : " + fileName + " from " + jarFileName);
		
		try {
			JarFile jar = new JarFile(jarFileName);
			Enumeration<JarEntry> e = jar.entries();
			while (e.hasMoreElements()) {
				JarEntry entry = (JarEntry) e.nextElement();
				if(entry.getName().endsWith(fileName)){
					InputStream is = jar.getInputStream(entry); // get the input stream
					int pos = entry.getName().lastIndexOf('\\'); // for windows
					if (pos == -1)
						pos = entry.getName().lastIndexOf('/'); // for unix
					if (pos == -1)
						pos = 0;
					FileOutputStream fos = new FileOutputStream(new File(outfolder).getAbsolutePath() + "/" + 
							entry.getName().substring(pos));
					while (is.available() > 0) {  // write contents of 'is' to 'fos'
						fos.write(is.read());
					}
					fos.close();
					is.close();
					return;
				}
				
			}


		} catch (IOException e) {			
			e.printStackTrace();
		}

	}
	// unjar is not tested
	public static void Unjar(String jarFileName, String outfolder){
		File inFile = new File(jarFileName);
		if (!inFile.exists())
			return;
		File outFolder = new File(outfolder);
		if (!outFolder.exists())
			outFolder.mkdirs();
		System.out.println("Extracting : " + jarFileName);
		
		try {
			JarFile jar = new JarFile(jarFileName);
			Enumeration<JarEntry> e = jar.entries();
			while (e.hasMoreElements()) {
				JarEntry entry = (JarEntry) e.nextElement();				
				File f = new File(outfolder + "/" + entry.getName());
				if (entry.isDirectory()) { // if its a directory, create it
					f.mkdir();
					continue;
				}
				InputStream is = jar.getInputStream(entry); // get the input stream
				FileOutputStream fos = new FileOutputStream(f);
				while (is.available() > 0) {  // write contents of 'is' to 'fos'
					fos.write(is.read());
				}
				fos.close();
				is.close();
			}


		} catch (IOException e) {			
			e.printStackTrace();
		}

	}
}
