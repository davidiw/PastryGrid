package ftc;

import java.io.IOException;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.past.PastContent;
import rice.p2p.past.rawserialization.PastContentDeserializer;

public class ContentDeserializer implements PastContentDeserializer {

	public PastContent deserializePastContent(InputBuffer buf,
			Endpoint endpoint, short contentType) throws IOException {
		switch (contentType) {
		case 30:
			return new RDVContent(buf);

		case 40:
			return new FTCContent(buf, endpoint);
		}
		return null;
	}
}
