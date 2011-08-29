package hre.util;

/**
 * This class contains a test report for a verification run.
 * 
 * @author Stefan Blom
 *
 */
public class TestReport {

  public static enum Verdict { Pass, Fail, Inconclusive, Error };
  
  private static Verdict verdict;
  
  public void setVerdict(Verdict verdict){
    this.verdict=verdict;
  }
  
  public Verdict getVerdict(){
    return verdict;
  }

}
