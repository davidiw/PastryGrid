package ftc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;
import rice.p2p.past.ContentHashPastContentHandle;
import rice.p2p.past.Past;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastContentHandle;
import rice.p2p.past.PastException;
import rice.p2p.past.rawserialization.RawPastContent;
import rice.pastry.Id;

public class RDVContent implements RawPastContent {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 30;

	private String filePath;
	byte file[];
	int length;

	public RDVContent(String filePath) throws IOException {
		this.filePath = filePath;
		File f = new File(filePath);
		length = (int) f.length();
		file = new byte[length];
		FileInputStream fo = new FileInputStream(f);
		fo.read(file);
	}

	public RDVContent(InputBuffer buf) throws IOException {
		filePath = buf.readUTF();
		length = buf.readInt();
		file = new byte[length];
		buf.read(file);
	}

	public void serialize(OutputBuffer buf) throws IOException {
		buf.writeUTF(filePath);
		buf.writeInt(length);
		buf.write(file, 0, file.length);
	}

	public short getType() {
		return TYPE;
	}

	public PastContent checkInsert(rice.p2p.commonapi.Id id,
			PastContent existingContent) throws PastException {

		if (existingContent == null) {
			return this;
		}
		if (existingContent instanceof RDVContent) {
			return this;
		} else {
			throw new PastException((new StringBuilder(
					"Content type collision. Inserting Content1 on topof "))
					.append(existingContent).toString());
		}
	}

	public PastContentHandle getHandle(Past local) {
		return new ContentHashPastContentHandle(local.getLocalNodeHandle(),
				getId());
	}

	public Id getId() {
		return Id.build(filePath);
	}

	public boolean isMutable() {
		return false;
	}

	public String toString() {
		return filePath;
	}

	/*
	 * public void result(String outFilePath)throws IOException{ File f = new
	 * File(outFilePath); FileOutputStream out = new FileOutputStream(f);
	 * out.write(file); out.close(); }
	 */

	public void saveFile(String name, String path) {
		if (path.endsWith("/") || path.endsWith("\\"))
			path = path.substring(0, path.length() - 1);
		new File(path).mkdirs();
		File f = new File(path + "/" + name);
		FileOutputStream out;
		try {
			out = new FileOutputStream(f);
			out.write(file);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}