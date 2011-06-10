
public interface IGForce {
	void pushAVCFrame(byte[] frame, int timecode);
	
	public ICudaListener getOutput() ;
	
	public void setOutput(ICudaListener output);
}
