package pack;

public class Demo {

	public static void main(String[] args) {
		MineSweeper m = MineSweeper.getInstance();
		LogicalAgent agent = new LogicalAgent(m);
		agent.play();

	}

}
