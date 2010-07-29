package principal;

import java.io.IOException;

import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;

public class Requirements {
	public int CPU;
	public int RAM;
	public String OS; // operating system

	public Requirements(int cPU, int rAM, String oS) {
		CPU = cPU;
		RAM = rAM;
		OS = oS;
	}

	public void serialize(OutputBuffer buf) throws IOException {
		buf.writeInt(CPU);
		buf.writeInt(RAM);
		buf.writeUTF(OS);
	}

	public static Requirements readRequirements(InputBuffer buf)
			throws IOException {
		int cpu = buf.readInt();
		int ram = buf.readInt();
		String os = buf.readUTF();
		return new Requirements(cpu, ram, os);
	}

}
