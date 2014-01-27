package input;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import times.Times;


public class MarkovIn {
	
	/* pointers to arrays of probabilities for each day saves some space */
	/* by not being allocated if markov weather not requested by user */
	/* alas, multi-dimensional arrays aren't so convenient */
	
	private double[] wetprob;	/* probability of being wet today */
	private double[] dryprob;	/* probability of being dry today */
	private double[] avg_ppt;	/* mean precip (cm) */
	private double[] std_ppt;	/* std dev. for precip */
	private double[][] u_cov;	/* mean temp (max, min) Celsius */
	private double[][][] v_cov;	/* covariance matrix */
	private int ppt_events;		/* number of ppt events generated this year */
	private boolean data;
	
	public MarkovIn() {
		this.data = false;
		this.wetprob = new double[Times.MAX_DAYS];
		this.dryprob = new double[Times.MAX_DAYS];
		this.avg_ppt = new double[Times.MAX_DAYS];
		this.std_ppt = new double[Times.MAX_DAYS];
		this.u_cov = new double[Times.MAX_WEEKS][2];
		this.v_cov = new double[Times.MAX_WEEKS][2][2];
	}
	public boolean onVerify() {
		if(this.data)
			return true;
		else
			return false;
	}
	
	public void onReadMarkov(Path MarkovProbabilityIn, Path MarkovCovarianceIn) throws IOException {
		onReadMarkovCovIn(MarkovCovarianceIn);
		onReadMarkovProbIn(MarkovProbabilityIn);
		this.data = true;
	}
	private void onReadMarkovProbIn(Path MarkovProbabilityIn) throws IOException {
		int nDays = 0;
		LogFileIn f = LogFileIn.getInstance();
		List<String> lines = Files.readAllLines(MarkovProbabilityIn, StandardCharsets.UTF_8);
		
		for (String line : lines) {
			//Do not go past MaxDays
			if(Times.MAX_DAYS == nDays)
				break;
			//Skip Comments and empty lines
			if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("[ \t]+");
				if(values.length < 5)
					f.LogError(LogFileIn.LogMode.ERROR, "swMarkov onReadMarkovProbIn : Line "+String.valueOf(lines.indexOf(line))+": Not enough values.");
				try {
					int day = Integer.parseInt(values[0])-1;
					this.wetprob[day] = Double.parseDouble(values[1]);
					this.dryprob[day] = Double.parseDouble(values[2]);
					this.avg_ppt[day] = Double.parseDouble(values[3]);
					this.std_ppt[day] = Double.parseDouble(values[4]);
				} catch(NumberFormatException e) {
					f.LogError(LogFileIn.LogMode.ERROR, "swMarkov onReadMarkovProbIn : Could not convert string to number." + e.getMessage());
				}
				
				nDays++;
			}
		}
		
	}
	private void onReadMarkovCovIn(Path MarkovCovarianceIn) throws IOException {
		int nWeeks = 0;
		LogFileIn f = LogFileIn.getInstance();
		List<String> lines = Files.readAllLines(MarkovCovarianceIn, StandardCharsets.UTF_8);
		
		for (String line : lines) {
			//Do not go past MaxWeeks
			if(Times.MAX_WEEKS == nWeeks)
				break;
			//Skip Comments and empty lines
			if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("[ \t]+");
				if(values.length < 7)
					f.LogError(LogFileIn.LogMode.ERROR, "swMarkov onReadMarkovCovIn : Line "+String.valueOf(lines.indexOf(line))+": Not enough values.");
				try {
					int week = Integer.parseInt(values[0])-1;
					this.u_cov[week][0] = Double.parseDouble(values[1]);
					this.u_cov[week][1] = Double.parseDouble(values[2]);
					this.v_cov[week][0][0] = Double.parseDouble(values[3]);
					this.v_cov[week][0][1] = Double.parseDouble(values[4]);
					this.v_cov[week][1][0] = Double.parseDouble(values[5]);
					this.v_cov[week][1][1] = Double.parseDouble(values[6]);
				} catch(NumberFormatException e) {
					f.LogError(LogFileIn.LogMode.ERROR, "swMarkov onReadMarkovCovIn : Could not convert string to number." + e.getMessage());
				}
				nWeeks++;
			}
		}
	}
	//TODO: finish Write functions
}
