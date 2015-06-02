package junitfootprint.footprint;

import org.eclipse.core.resources.IFile;

public class FootPrintInfo {

	private String typeName;
	private String methodName;
	private boolean isSuccess;
	private IFile iFile;
	private String time;

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}


	public IFile getiFile() {
		return iFile;
	}

	public void setiFile(IFile iFile) {
		this.iFile = iFile;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

}
