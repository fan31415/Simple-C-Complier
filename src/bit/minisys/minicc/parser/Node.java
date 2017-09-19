package bit.minisys.minicc.parser;

public class Node {
	private String nodeName;
	private String plainValue;
	//if valid is false, then failed
	private boolean valid;
	
	public Node(Grammar g) {
		
	}
	public Node(String nodeName) {
		this.nodeName = nodeName;
	}
	public Node(String nodeName, String value) {
		this.nodeName = nodeName;
		this.plainValue = value;
	}
	public String getNodeName() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	public String getPlainValue() {
		return plainValue;
	}
	public void setPlainValue(String plainValue) {
		this.plainValue = plainValue;
	}
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	
}
