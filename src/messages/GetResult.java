package messages;

import java.io.File;
import java.io.IOException;

import principal.ApplicationPastryGrid;
import principal.NodePastryGrid;
import principal.TaskPastryGrid;

import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;
import zip.Zip;

public class GetResult extends MessagePastryGrid {

	private static final long serialVersionUID = 1L;
	public static final short TYPE = 6;
	TaskPastryGrid task;

	public GetResult(NodeHandle from, String appName, String time,
			TaskPastryGrid task) {
		super(from, appName, time);
		this.task = task;
	}

	public GetResult(InputBuffer buf, Endpoint endpoint) throws IOException {
		super(buf, endpoint);

		task = TaskPastryGrid.readTaskPastryGrid(buf);
	}

	public void serialize(OutputBuffer buf) throws IOException {
		super.serialize(buf);

		task.serialize(buf);
	}

	public void response(ApplicationPastryGrid App) {
		String xmlFilePath = NodePastryGrid.workDirectory + appName + time + "/"
				+ task + "/Task.xml";
		File outputFile = new File(NodePastryGrid.workDirectory + appName + time + "/"
				+ task + "/" + task.getOutputFile(xmlFilePath));
		String outputFilePath = null;
		TaskPastryGrid taskVerifier; // it takes an empty name when we can't
										// recuperate the outputfilepath
		if (outputFile.exists()) {
			taskVerifier = task;
			String[] filesPath = { outputFile.getAbsolutePath() };
			outputFilePath = NodePastryGrid.workDirectory + appName + time + "/"
					+ task + "/" + "output.zip";
			Zip.zipFiles(outputFilePath, filesPath);
		} else
			taskVerifier = new TaskPastryGrid();

		MyResult myresult = new MyResult(App.NPG.node.getLocalNodeHandle(),
				appName, time, taskVerifier, outputFilePath, false);
		//App.routeMyMsgDirect(myresult, from);
		App.sendFile(myresult, from);

	}

	public short getType() {
		return TYPE;
	}
}