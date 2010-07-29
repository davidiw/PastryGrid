package principal;

import org.mpisws.p2p.filetransfer.BBReceipt;
import org.mpisws.p2p.filetransfer.FileReceipt;
import org.mpisws.p2p.filetransfer.FileTransferListener;
import org.mpisws.p2p.filetransfer.Receipt;

class FileListenerPastryGrid implements FileTransferListener {
    public void fileTransferred(FileReceipt receipt,
        long bytesTransferred, long total, boolean incoming) {
      String s;
      if (incoming) {
        s = " Downloaded ";
      } else {
        s = " Uploaded ";              
      }
      double percent = 100.0*bytesTransferred/total;
      
      System.out.println(s+Math.round(percent)+"% of "+receipt.getFile().length());
    }

    public void msgTransferred(BBReceipt receipt, int bytesTransferred,
        int total, boolean incoming) {
      String s;
      if (incoming) {
        s = " Downloaded ";
      } else {
        s = " Uploaded ";              
      }
      double percent = 100.0*bytesTransferred/total;
      System.out.println(s+Math.round(percent)+"% of "+receipt.getSize());
    }

    public void transferCancelled(Receipt receipt, boolean incoming) {
      String s;
      if (incoming) {
        s = "download";
      } else {
        s = "upload";              
      }
      System.out.println(": Cancelled "+s+" of "+receipt.getSize());
    }
    
    public void transferFailed(Receipt receipt, boolean incoming) {
      String s;
      if (incoming) {
        s = "download";
      } else {
        s = "upload";              
      }
      System.out.println(": Transfer Failed "+s+" of "+receipt.getSize());
    }
  }