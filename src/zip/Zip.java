package zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {
	public static void zipFiles(String zipFileName, String[] FilesName) {
		byte[] tmpBuf = new byte[1024];
		ZipOutputStream out;
		try {
			out = new ZipOutputStream(new FileOutputStream(zipFileName));
			System.out.println("Creating : " + zipFileName);
			for (int i = 0; i < FilesName.length; i++) {
				File file = new File(FilesName[i]);
				if (file.exists()) {
					//File Dst = new File(file.getName());
					//copyDirectory(file, Dst);

					if (file.isDirectory())
						addDir("",file, out);
					else {
						FileInputStream in = new FileInputStream(file.getPath());
						// System.out.println(" Adding: " + file.getPath());

						out.putNextEntry(new ZipEntry(file.getName()));

						// Transfer from the file to the ZIP file
						int len;
						while ((len = in.read(tmpBuf)) > 0) {
							out.write(tmpBuf, 0, len);
						}

						// Complete the entry
						out.closeEntry();
						in.close();
					}
					//deleteDir(Dst);
				} else
					System.out.println("not found " + FilesName[i]);
			}
			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void zipDir(String zipFileName, String dir) {
		File dirObj = new File(dir);
		if (!dirObj.isDirectory()) {
			System.err.println(dir + " is not a directory");
			System.exit(1);
		}

		//File Dst = new File(dirObj.getName());
		//copyDirectory(dirObj, Dst);

		try {
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
					zipFileName));
			System.out.println("Creating : " + zipFileName);
			addDir(dirObj.getName()+"/", dirObj, out);//addDir(Dst, out);
			// Complete the ZIP file
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		//deleteDir(Dst);
	}

	private static void addDir(String path, File dirObj, ZipOutputStream out)
			throws IOException {
		File[] files = dirObj.listFiles();
		byte[] tmpBuf = new byte[1024];

		if(files.length == 0){
			//System.out.println(" Adding: " + path);
			out.putNextEntry(new ZipEntry(path+""));
			out.write(0);
			// Complete the entry
			out.closeEntry();
			return;
		}
		
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				addDir(path+files[i].getName()+"/", files[i], out);
				continue;
			}

			FileInputStream in = new FileInputStream(files[i].getPath());
			//System.out.println(" Adding: " + path+files[i].getName());

			out.putNextEntry(new ZipEntry(path+files[i].getName()));

			// Transfer from the file to the ZIP file
			int len;
			while ((len = in.read(tmpBuf)) > 0) {
				out.write(tmpBuf, 0, len);
			}

			// Complete the entry
			out.closeEntry();
			in.close();
		}
	}

	public static void copyDirectory(File sourceLocation, File targetLocation) {
		try {
			if (sourceLocation.isDirectory()) {
				if (!targetLocation.exists()) {
					targetLocation.mkdirs();
				}
				String[] children = sourceLocation.list();
				for (int i = 0; i < children.length; i++) {
					File srcLoc = new File(sourceLocation, children[i]);
					File dstLoc = new File(targetLocation, children[i]);
					if (srcLoc.isDirectory())
						dstLoc.mkdirs();
					copyDirectory(srcLoc, dstLoc);
				}
			} else {

				InputStream in = new FileInputStream(sourceLocation);
				OutputStream out = new FileOutputStream(targetLocation
						.getAbsolutePath());
				// System.out.println(targetLocation.getAbsolutePath());
				// Copy the bits from instream to outstream
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static void copyFile(File sourceLocation, File outFolder) {
		try {
			if (sourceLocation.exists()) {
				if (!outFolder.exists())
					outFolder.mkdirs();

				File targetLocation = new File(outFolder.getAbsolutePath()
						+ "/" + sourceLocation.getName());
				InputStream in = new FileInputStream(sourceLocation);
				OutputStream out = new FileOutputStream(targetLocation
						.getAbsolutePath());
				// Copy the bits from instream to outstream
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
			} else
				System.out.println(sourceLocation.getAbsolutePath()
						+ " not found");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}

}
