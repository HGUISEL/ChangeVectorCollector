package change.vector.collector;

import change.vector.collector.Input;
import change.vector.collector.Collector;
import change.vector.collector.ChangeVector;
import java.util.ArrayList;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class Main {
	Input input = null;
	boolean help = false;
	boolean is_repo = false;
	boolean is_local = false;
	boolean is_correlation = false;

	public static void main(String[] args) throws Exception {
		Main cvc = new Main();
		cvc.run(args);
	}
		
	public void run(String[] args) throws Exception {
		Options options = createOptions();
		ArrayList<BeforeBIC> bbics = new ArrayList<BeforeBIC>();
		
		if(parseOptions(options, args)) {
			if(help) {
				printHelp(options);
				return;
			}
			
			// compute correlation					-c
			if(is_correlation) {
				Correlation.computeAll(input);
				return;
			}
				
			// collect bbic from git repository		-r
			if(is_repo)
				bbics = Collector.collectBeforeBIC(input);
			
			// collect bbic from csv file			-l
			if(is_local)
				bbics = Collector.collectBeforeBICFromLocalFile(input);
			
			// collect java files of bbic of bic
			Collector.collectFiles(input, bbics);
			
			// perform Gumtree to retrieve change vector
			ChangeVector.runGumtreeDIST(input);
		}
	}
	
	private boolean parseOptions(Options options, String[] args) {
		CommandLineParser parser = new DefaultParser();
		String in;
		String out;
		String url;
		
		try {
			CommandLine cmd = parser.parse(options, args);
			try {

				if (cmd.hasOption("c"))	is_correlation = true;
				else if (cmd.hasOption("r")) is_repo = true;
				else if (cmd.hasOption("l")) is_local = true;
				in = cmd.getOptionValue("i");
				out = cmd.getOptionValue("o");
				url = cmd.getOptionValue("u");

			} catch (Exception e) {
				System.out.println(e.getMessage());
				printHelp(options);
				return false;
			}

			input = new Input(url, in, out);
		} catch (Exception e) {
			e.printStackTrace();
			printHelp(options);
			return false;
		}

		return true;
	}
	
	private Options createOptions() {
		Options options = new Options();

		options.addOption(Option.builder("c").longOpt("correlation")
				.desc("Computes correlation of each vectors")
				.build());
		
		options.addOption(Option.builder("r").longOpt("repo")
				.desc("Collect change vectors straight from Git repository")
				.build());
		
		options.addOption(Option.builder("l").longOpt("local")
				.desc("Collect change vectors with BBIC file in local")
				.build());
		
		options.addOption(Option.builder("u").longOpt("url")
				.desc("url of the git repo").hasArg()
				.argName("input_path").required().build());
		
		options.addOption(Option.builder("i").longOpt("input")
				.desc("directory of the input file to parse").hasArg()
				.argName("input_path").required().build());
		
		options.addOption(Option.builder("o").longOpt("output")
				.desc("directory will have result file").hasArg()
				.argName("output_path").required().build());

		options.addOption(Option.builder("h").longOpt("help").desc("Help").build());

		return options;
	}
	
	private void printHelp(Options options) {
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		String header = "Collects change vectors from 2 files";
		String footer = "\nPlease report issues at https://github.com/HGUISEL/ChangeVectorCollector/issues";
		formatter.printHelp("ChangeVectorCollector", header, options, footer, true);
	}
}

