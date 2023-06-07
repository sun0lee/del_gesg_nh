package com.gof.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.ejml.data.Complex_F64;
import org.ejml.simple.SimpleEVD;
import org.ejml.simple.SimpleMatrix;

import com.gof.entity.IrCurveSpot;
import com.gof.entity.IrCurveYtm;
import com.gof.interfaces.Constant;
import com.gof.model.entity.SmithWilsonRslt;
import com.gof.util.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@Getter
@Setter
@Slf4j
public abstract class IrModel implements Constant {	
                                       
	public static final double                 ZERO_DOUBLE          = 0.0000001;	
	public static final double                 SIMPLEX_STEP_MIN     = 1e-5;	
	public static final int                    DECIMAL_DIGIT        = 17;
	public static final int                    RANDOM_SEED          = 470;
	public static final int                    SCEN_NUM             = 200;
                                        
	protected LocalDate                        baseDate;
	protected String                           irCurveId;
	protected int                              modelType;
	protected char                             cmpdType             = CMPD_MTD_DISC;
	protected char                             timeUnit             = TIME_UNIT_YEAR;
	protected int                              dayCountBasis        = DCB_MON_DIF;		
	protected boolean                          isRealNumber;
	                                           
	protected double[]                         tenor;
	protected LocalDate[]                      tenorDate;
	protected double[]                         tenorYearFrac;	
	protected double[]                         iRateBase;
	                                           
	protected LocalDate[]                      iRateDateHis;	
	protected double[][]                       iRateHis;	
	
	protected Map<Double, Double>              termStructureBase    = new TreeMap<Double, Double>();	
	protected Map<String, Map<Double, Double>> termStructureHis     = new TreeMap<String, Map<Double, Double>>();	
	

	public void setTermStructureBase(List<IrCurveSpot> irCurveHisList) {
		
//		this.tenor = irCurveHis.stream().map(s -> Double.parseDouble(s.getMatCd().substring(1, 5)) / 12.0).mapToDouble(Double::doubleValue).toArray();
		for(IrCurveSpot curve : irCurveHisList) this.termStructureBase.put(Double.parseDouble(curve.getMatCd().substring(1, 5)) / MONTH_IN_YEAR, curve.getSpotRate());
		setTermStructureBase(this.termStructureBase);
		

//		setTermStructureBase(irCurveHisList.stream().collect(Collectors.toMap(IrCurveSpot::getMatYear, IrCurveSpot::getSpotRate, (s,u)->s, TreeMap::new)));
	}
	

	public void setTermStructureYtm(List<IrCurveYtm> ytmCurveHisList) {	

		for(IrCurveYtm curve : ytmCurveHisList) this.termStructureBase.put(Double.parseDouble(curve.getMatCd().substring(1, 5)) / MONTH_IN_YEAR, curve.getYtm());
		setTermStructureBase(this.termStructureBase);							
		
//		setTermStructureBase(ytmCurveHisList.stream().collect(Collectors.toMap(IrCurveYtm::getMatYear, IrCurveYtm::getYtm, (s,u)->s, TreeMap::new)));
				
	}	

	
	public void setTermStructureBase(Map<Double, Double> termStructureBase) {
	
		this.termStructureBase = termStructureBase;
		this.tenor = this.termStructureBase.keySet().stream().mapToDouble(Double::doubleValue).toArray();
		this.iRateBase = this.termStructureBase.values().stream().mapToDouble(Double::doubleValue).toArray();
	}
	
	
	protected double zeroBondUnitPrice(double rateCont, double mat) {
		return Math.exp(-rateCont * mat);
	}
	
	
	protected static double irDiscToCont(double discRate) {
		return Math.log(1.0 + discRate);
	}
	
	
	protected static double[] irDiscToCont(double[] discRate) {
		
		double[] contRate = new double[discRate.length]; 
		
		for(int i=0; i<discRate.length; i++) {
			contRate[i] = irDiscToCont(discRate[i]);
		}
		
		return contRate;
	}
	
	
	protected static double[][] irDiscToCont(double[][] discRate) {
		
		double[][] contRate = new double[discRate.length][discRate[0].length]; 
		
		for(int i=0; i<discRate.length; i++) {
			for(int j=0; j<discRate[0].length; j++) {
				contRate[i][j] = irDiscToCont(discRate[i][j]);	
			}			
		}
		
		return contRate;
	}
	
	
	protected static double irContToDisc( double contRate) {
		return Math.exp(contRate) - 1.0;
	}
	

	protected static double[] irContToDisc(double[] contRate) {
		
		double[] discRate = new double[contRate.length]; 
		
		for(int i=0; i<contRate.length; i++) {
			discRate[i] = irContToDisc(contRate[i]);
		}
		
		return discRate;
	}
	
	
	protected static double[][] irContToDisc(double[][] contRate) {
		
		double[][] discRate = new double[contRate.length][contRate[0].length]; 
		
		for(int i=0; i<contRate.length; i++) {
			for(int j=0; j<contRate[0].length; j++) {
				discRate[i][j] = irContToDisc(contRate[i][j]);	
			}			
		}		
		
		return discRate;
	}
	
	//TODO: biz method
	public static TreeMap<String, Double> irSpotDiscToFwdM1Map(TreeMap<String, Double> spotRateMap) {
		
		TreeMap<String, Double> fwdRateMap = new TreeMap<String, Double>();				
		
		String[] matCd    = spotRateMap.entrySet().stream().map(s -> s.getKey()).toArray(String[]::new);
		double[] tenor    = spotRateMap.entrySet().stream().map(s -> Double.valueOf(1.0 * Integer.valueOf(s.getKey().substring(1)) / MONTH_IN_YEAR)).mapToDouble(Double::doubleValue).toArray();
		double[] spotCont = irDiscToCont(spotRateMap.entrySet().stream().map(s -> s.getValue()).mapToDouble(Double::doubleValue).toArray());
		double[] fwdCont  = new double[spotCont.length];
		double[] fwdDisc  = new double[spotCont.length];
		
		for(int i=0; i<matCd.length; i++) {						
			fwdCont[i] = (i > 0) ? (spotCont[i] * tenor[i] - spotCont[i-1] * tenor[i-1]) / (tenor[i] - tenor[i-1]) : spotCont[i];
			fwdDisc[i] = irContToDisc(fwdCont[i]);
			fwdRateMap.put(matCd[i], fwdDisc[i]);
		}		
		return fwdRateMap;
	}
	
	
	public static TreeMap<String, Double> irSpotDiscToFwdMap(String bssd, TreeMap<String, Double> spotRateMap, String fwdMatCd) {		
		
		TreeMap<String, Double> fwdRateMap = new TreeMap<String, Double>();
		TreeMap<Double, Double> tsMap = new TreeMap<Double, Double>();
		
		LocalDate baseDate = DateUtil.convertFrom(bssd).with(TemporalAdjusters.lastDayOfMonth());		
		double ltfr        = spotRateMap.lastEntry().getValue();
		int    ltfrT       = Double.valueOf(1.0 * Integer.valueOf(spotRateMap.lastKey().substring(1)) / MONTH_IN_YEAR).intValue();
		int    llp         = ltfrT;		
		int    fwdIdx      = Integer.valueOf(fwdMatCd.substring(1));
		fwdIdx             = fwdIdx < 0 ? 0 : fwdIdx;
//		double fwdTenor    = Double.valueOf(1.0 * Integer.valueOf(fwdMatCd.substring(1)) / MONTH_IN_YEAR);
		
		double[] tenor     = spotRateMap.entrySet().stream().map(s -> Double.valueOf(1.0 * Integer.valueOf(s.getKey().substring(1)) / MONTH_IN_YEAR)).mapToDouble(Double::doubleValue).toArray();
		double[] spotDisc  = spotRateMap.entrySet().stream().map(s -> s.getValue()).mapToDouble(Double::doubleValue).toArray();
		
		for(int i=0; i<tenor.length; i++) tsMap.put(tenor[i], spotDisc[i]);
		
		//for Convinicence, Maxmimum Forward Term is set to 100yrs. It can be set to global variable of projectYear 
		SmithWilsonKics sw = new SmithWilsonKics(baseDate, tsMap, CMPD_MTD_DISC, true, ltfr, ltfrT, llp, 1, 100, DCB_MON_DIF);		
	    List<SmithWilsonRslt> swRsltList = sw.getSmithWilsonResultList();	    
	    
//		swRsltList.stream().filter(s-> Double.parseDouble(s.getMatCd().substring(1, 5)) % 1 == 0)
//        				   .forEach(s->log.info("{}, {}, {} , {}, {}, {}, {}", s.getBaseDate(), s.getResultType(), s.getMatCd(), s.getSpotDisc(), s.getSpotCont(), s.getFwdDisc(), s.getDcntFactor(), s.getMatTerm()));		

	    String[] prjMatCd    = swRsltList.stream().map(s -> s.getMatCd()   ).toArray(String[]::new);
	    double[] prjTenor    = swRsltList.stream().map(s -> s.getMatTerm() ).mapToDouble(Double::doubleValue).toArray();
	    double[] prjSpotCont = swRsltList.stream().map(s -> s.getSpotCont()).mapToDouble(Double::doubleValue).toArray();
	    
	    double[] shiftFwdCont  = new double[prjMatCd.length];
	    double[] shiftFwdDisc  = new double[prjMatCd.length];

	    if(fwdIdx > 0) {
//		    (t2*spot(t2) - t1*spot(t1)) / (t2 - t1)
			for(int i=0; i<prjMatCd.length; i++) {			 
				shiftFwdCont[i] = (i+fwdIdx < prjMatCd.length) ? (prjSpotCont[i+fwdIdx] * prjTenor[i+fwdIdx] - prjSpotCont[fwdIdx-1] * prjTenor[fwdIdx-1]) / (prjTenor[i+fwdIdx] - prjTenor[fwdIdx-1]) : shiftFwdCont[i-1];
				shiftFwdDisc[i] = irContToDisc(shiftFwdCont[i]);
			}	    	
	    }
	    else {
			for(int i=0; i<prjMatCd.length; i++) {			 
				shiftFwdCont[i] = prjSpotCont[i];
				shiftFwdDisc[i] = irContToDisc(shiftFwdCont[i]);
			}	    	
	    }
	    
	    for(int i=0; i<prjMatCd.length; i++) {
//	    	log.info("{}, {}, {}, {}", prjMatCd[i], prjSpotCont[i], shiftFwdCont[i], shiftFwdDisc[i]);
	    	if(spotRateMap.containsKey(prjMatCd[i])) {
//	    		log.info("fwdMap: {}, {}", prjMatCd[i], shiftFwdDisc[i]);
	    		fwdRateMap.put(prjMatCd[i],  shiftFwdDisc[i]);
	    	}
	    }		
		return fwdRateMap;
	}		
	
	
	public static double round(double number, int decimalDigit) {
		if(decimalDigit < 0) return Math.round(number);
		return Double.parseDouble(String.format("%." + decimalDigit + "f", number));
	}
	
	
	public static double round(double number) {
		return Double.parseDouble(String.format("%." + DECIMAL_DIGIT + "f", number));
	}
	
	
	public static int compareDbltoInt(double val1, int val2) {		
		
		if(Math.abs(val1 - val2) < ZERO_DOUBLE) return 0;
		else return ((val1 - val2 > 0) ? 1 : -1);		
	}

	
	protected static double[][] vecToMat(double[] vec) {
		
		double[][] mat = new double[vec.length][1];		
		for(int i=0; i<mat.length; i++) mat[i][0] = vec[i];
		return mat;		
	}	
	
	
	protected static double[] matToVec(double[][] mat, int colIdx) {		
		
		double[] col = new double[mat.length];		
		for(int i=0; i<col.length; i++) col[i] = mat[i][colIdx];
		return col;
	}				
	
	
	protected static double sumVector(double[] vec) {
		
		double sum = 0.0;
		for(int i=0; i<vec.length; i++) sum += vec[i];
		return sum;
	}		

		
	protected static double[][] toDiagMatrix(double... elements) {
		
		double[][] diagMat = new double[elements.length][elements.length];
		
		for(int i=0; i<diagMat.length; i++) {
			for(int j=0; j<diagMat[i].length; j++) {
				diagMat[i][j] = (i==j) ? elements[i] : 0.0;
			}
		}
		return diagMat;
	}
	
	
	protected static double[][] toIdentityMatrix(int dim) {
		return toDiagMatrix(1.0, dim);
	}
	
	
	protected static double[][] toDiagMatrix(double element, int dim) {
		
		double[][] diagMat = new double[dim][dim];
		
		for(int i=0; i<diagMat.length; i++) {
			for(int j=0; j<diagMat[i].length; j++) {
				diagMat[i][j] = (i==j) ? element : 0.0;
			}
		}
		return diagMat;		
	}		
	
	
	protected static double[] toVector(double element, int dim) {
		
		double[] vector = new double[dim];		
		for(int i=0; i<vector.length; i++) vector[i] = element;
		return vector;		
	}	
		
	
	protected static double[] matToVecMean(double[][] mat) {
		
		double[] mean = new double[mat.length];		
		
		for(int i=0; i<mat.length; i++) {
			double sum = 0.0;			
			for(int j=0; j<mat[i].length; j++) {
				sum = sum + mat[i][j];				
			}
			mean[i] = sum / mat[i].length;
		}		
		return mean;
	}
	
	
	protected static double[] matToVecStdError(double[][] mat, double size) {

		double[] se = new double[mat.length];
		
		for(int i=0; i<se.length; i++) {
			se[i] = vectSd(mat[i]) / Math.sqrt(size);
		}		
		return se;
	}
	
	
	protected static double[] matToVecSd(double[][] mat) {		
		return matToVecStdError(mat,  1.0);
	}	

	
	protected static double nearValue(double[] vec, double value) {		
		
		double min = Double.MAX_VALUE;
		double val = value;
		
		for(int i=0; i<vec.length; i++) {
			double diff = Math.abs(vec[i] - value);
			if(diff < min) {
				min = diff;
				val = vec[i];				
			}
		}
		return val;		
	}
	
	protected static double[][] toLowerTriangular3(double[] elements) {
		
		if(elements.length != 6) {
			log.error("Check the elements in Sigma Matrix");
			return null;
		}
		
		double[][] lowerMat = new double[3][3];
		
		lowerMat[0][0] = elements[0];  lowerMat[0][1] = 0.0;          lowerMat[0][2] = 0.0;
		lowerMat[1][0] = elements[1];  lowerMat[1][1] = elements[2];  lowerMat[1][2] = 0.0;
		lowerMat[2][0] = elements[3];  lowerMat[2][1] = elements[4];  lowerMat[2][2] = elements[5];
		
		return lowerMat;
	}
	

	protected static Map<Integer, List<Double>> eigenValueUserDefined(double[][] mat, int dim) {
		return eigenValueUserDefined(new SimpleMatrix(mat), dim);		
	}
	
	
	protected static Map<Integer, List<Double>> eigenValueOrigin(SimpleMatrix mat, int dim) {
		
		SimpleEVD<SimpleMatrix> eigmat = mat.eig();				
		Complex_F64 [] eigval = eigmat.getEigenvalues().stream().toArray(Complex_F64[]::new);			
		SimpleMatrix[] eigvec = new SimpleMatrix[] {eigmat.getEigenVector(0), eigmat.getEigenVector(1), eigmat.getEigenVector(2)};		
		
		Double[][] eigvec_user = new Double[dim][dim];		
		
		for(int j=0; j<dim; j++) {						
			eigvec_user[0][j] = eigvec[0].get(j,0);
			eigvec_user[1][j] = eigvec[1].get(j,0);
			eigvec_user[2][j] = eigvec[2].get(j,0);			
		}			
		
		Map<Double, List<Double>> eigMap = new TreeMap<Double,  List<Double>>(Collections.reverseOrder());		
		for(int i=0; i<dim; i++) eigMap.put(eigval[i].getReal(), Arrays.asList(eigvec_user[i]));		
		
		int i=0;
		Map<Integer, List<Double>> eigvMap = new TreeMap<Integer, List<Double>>();
		for(Map.Entry<Double, List<Double>> eig : eigMap.entrySet()) {
			eigvMap.put(i,  eig.getValue());
			i++;
		}		
		return eigvMap;
	}
	
	
	//TODO: for Symmetric Matrix	
	protected static Map<Integer, List<Double>> eigenValueUserDefined(SimpleMatrix mat, int dim) {
				
		if(dim != 3) return null;
		if(!mat.isIdentical(mat.transpose(), 0.000000001)) return null;
	
		SimpleEVD<SimpleMatrix> eigmat = mat.eig();				
		Complex_F64 [] eigval = eigmat.getEigenvalues().stream().toArray(Complex_F64[]::new);			
		SimpleMatrix[] eigvec = new SimpleMatrix[] {eigmat.getEigenVector(0), eigmat.getEigenVector(1), eigmat.getEigenVector(2)};		
		
//		Complex_F64    prod   = eigval[0].times(eigval[1].times(eigval[2]));		
//		log.info("{}, {}", prod, mat.determinant());
//		log.info("{}, {}, {}, {}", eigval[0], eigval[1], eigval[2]);
//		log.info("{}, {}, {}, {}", eigvec[0], eigvec[1], eigvec[2]);
		
		SimpleMatrix mat_temp1 = mat.minus(SimpleMatrix.identity(dim).scale(eigval[1].getReal()));		
		SimpleMatrix mat_temp2 = mat.minus(SimpleMatrix.identity(dim).scale(eigval[2].getReal()));
		
//		log.info("{}, {}", mat_temp1, mat_temp2);
		
//		 (2,1), (2,2),  |  (1,1), (1,2),  |  (1,1), (1,2)
//		 (3,1), (3,2),  |  (3,1), (3,2),  |  (2,1), (2,2)		
		double[] eig_temp1 = new double[] { +1 * mat_temp1.extractMatrix(1, 3, 0, 2).determinant()                
				                          , -1 * mat_temp1.extractMatrix(0, 1, 0, 2).combine(1, 0, mat_temp1.extractMatrix(2, 3, 0, 2)).determinant()
				                          , +1 * mat_temp1.extractMatrix(0, 2, 0, 2).determinant() };
		
		
        double[] eig_temp2 = new double[] { +1 * mat_temp2.extractMatrix(1, 3, 0, 2).determinant()
                                          , -1 * mat_temp2.extractMatrix(0, 1, 0, 2).combine(1, 0, mat_temp2.extractMatrix(2, 3, 0, 2)).determinant()
                                          , +1 * mat_temp2.extractMatrix(0, 2, 0, 2).determinant() };
     
        double sumsq_temp1 = 0.0, sumsq_temp2 = 0.0;
		for(int i=0; i<dim; i++) {
			sumsq_temp1 += Math.pow(eig_temp1[i], 2);
			sumsq_temp2 += Math.pow(eig_temp2[i], 2);
		}	  

		Double[][] eigvec_user = new Double[dim][dim];		
		
		for(int j=0; j<dim; j++) {						
			eigvec_user[0][j] = eigvec[0].get(j,0);
			eigvec_user[1][j] = eig_temp1[j] / Math.sqrt(sumsq_temp1);
			eigvec_user[2][j] = eig_temp2[j] / Math.sqrt(sumsq_temp2);			
		}				

		Map<Double, List<Double>> eigMap = new TreeMap<Double,  List<Double>>(Collections.reverseOrder());		
		for(int i=0; i<dim; i++) eigMap.put(eigval[i].getReal(), Arrays.asList(eigvec_user[i]));		
		
		int i=0;
		Map<Integer, List<Double>> eigvMap = new TreeMap<Integer, List<Double>>();
		for(Map.Entry<Double, List<Double>> eig : eigMap.entrySet()) {
			eigvMap.put(i,  eig.getValue());
			i++;
		}		
		return eigvMap;
	}		
	
	
	protected static List<IrCurveSpot> setIrCurveHisInt(String[] dateHis, String[] matCd, double[][] iRateHis) {
		
		List<IrCurveSpot> curveHis = new ArrayList<IrCurveSpot>();
		
		for(int i=0; i<dateHis.length; i++) {
			for(int j=0; j<matCd.length; j++) {
				IrCurveSpot crv = new IrCurveSpot();
				crv.setBaseDate(dateHis[i]);
				crv.setIrCurveId("1111111");
				crv.setMatCd(matCd[j]);
				crv.setSpotRate(iRateHis[i][j]);
				curveHis.add(crv);				
			}
		}		
		return curveHis;
	}

	
	protected static List<IrCurveSpot> setIrCurveHisBase(String baseDate, String[] matCd, double[] baseRate) {
		
		List<IrCurveSpot> curveHis = new ArrayList<IrCurveSpot>();
		
		for(int i=0; i<matCd.length; i++) {
			IrCurveSpot crv = new IrCurveSpot();
			crv.setBaseDate(baseDate);
			crv.setIrCurveId("1111111");
			crv.setMatCd(matCd[i]);
		    crv.setSpotRate(baseRate[i]);
		    curveHis.add(crv);
		}		
		return curveHis;
	}
		


	protected static double getTimeFactor(LocalDate date1, LocalDate date2, int dayCountBasis) {			      
	    
	    switch(dayCountBasis) {
	    
	        case DCB_ACT_365: return ChronoUnit.DAYS.between(date1, date2) / 365.0;
	        case DCB_A30_360: return daysBetweenA30360(date1, date2) / 360.0;
	        case DCB_E30_360: return daysBetweenE30360(date1, date2) / 360.0;
	        case DCB_ACT_ACT: return getTimeFactorActAct(date1, date2);
            case DCB_ACT_360: return ChronoUnit.DAYS.between(date1, date2) / 360.0;          
            case DCB_MON_DIF: return ChronoUnit.MONTHS.between(date1.withDayOfMonth(1), date2.withDayOfMonth(1)) * 1.0 / MONTH_IN_YEAR;
	        default: return 0.0;	        
	    }	    
	}
	
	
    private static double getTimeFactorActAct(LocalDate date1, LocalDate date2) {    	

    	double timeFactor;    	
    	
    	timeFactor = (double) (date1.lengthOfYear() - date1.getDayOfYear()) / date1.lengthOfYear();    	
    	timeFactor += (double) date2.getDayOfYear() / date2.lengthOfYear();    	
    	timeFactor += (double) date2.getYear() - date1.getYear() - 1.0;
    	
    	//System.out.println("TF(act/act) = " + timeFactor + " : " + date1 + " | " + date2);
    	return timeFactor;    	
    }   
    
    
    private static int daysBetweenA30360(LocalDate date1, LocalDate date2) {    	
    	
    	int day1 = date1.getDayOfMonth();
    	int day2 = date2.getDayOfMonth();
    	int daysDiff;
    	
    	daysDiff = (date2.getYear() - date1.getYear()) * 12;
    	daysDiff += date2.getMonthValue() - date1.getMonthValue();
    	daysDiff *= 30;
    	
    	if (date1.getMonth().equals(Month.FEBRUARY) && day1 == date1.lengthOfMonth()) {
    		if(date2.getMonth().equals(Month.FEBRUARY) && day2 == date2.lengthOfMonth()) {
    			day2 = 30;    			
    		}
    		day1 = 30;
    	}
    	
    	if (day2 == 31 && day1 >= 30) day2 = 30;
    	if (day1 == 31) day1 = 30;
    	
    	daysDiff += day2 - day1;
    	
    	return daysDiff;
    }
    
    
    private static int daysBetweenE30360(LocalDate date1, LocalDate date2) {    	
    	
    	int day1 = date1.getDayOfMonth();
    	int day2 = date2.getDayOfMonth();
    	int daysDiff;
    	
    	daysDiff = (date2.getYear() - date1.getYear()) * 12;
    	daysDiff += date2.getMonthValue() - date1.getMonthValue();
    	daysDiff *= 30;
    	
    	if (day1 > 30) day1 = 30;
    	if (day2 > 30) day2 = 30;
    	
    	daysDiff += day2 - day1;
    	
    	return daysDiff;
    }
    	

    public static LocalDate stringToDate(String dateString) {
    	
		if(dateString != null && dateString.length() == 8) {
			
			int year  = Integer.parseInt(dateString.substring(0, 4));
			int month = Integer.parseInt(dateString.substring(4, 6));
			int day   = Integer.parseInt(dateString.substring(6, 8));    		
			
			return LocalDate.of(year, month, day);    		
		}
		return null;    	
    }
    
    
    public static String dateToString(LocalDate date) {    	
    	
    	if(date != null) {    		
    		
    		return    String.format("%04d", date.getYear())
    				+ String.format("%02d", date.getMonthValue())
    				+ String.format("%02d", date.getDayOfMonth());
    	}    	
    	return null;    	
    }
    
    
	protected static double[][] getIntRate(String path) throws Exception {		
				
		String[][] input = readCSVtoArray(path);
		
//		log.info("{}", input[0][13]);		
		double[][] intRate = new double[input.length][input[0].length-1];
		
		for(int i=0; i<intRate.length; i++) {
			for(int j=0; j<intRate[0].length; j++) {				
				intRate[i][j] = Double.parseDouble(input[i][j+1]);		
//				log.info("{}", intRate[i][j]);
			}
		}		
		return intRate;		
	}
	
	protected static String[] getIntDate(String path) throws Exception {		
		
		String[][] input = readCSVtoArray(path);		
		
		String[] date = new String[input.length];
		
		for(int i=0; i<date.length; i++) {
			date[i] = input[i][0];
//			log.info("{}", date[i]);
		}
		
		return date;		
	}
		
	
	public static String[][] readCSVtoArray(String path) throws IOException {     // to be checked(debugging)
		
		ArrayList<ArrayList<String>> getCSVtoList = readCSVtoList(path);
		String[][] getCSVtoArray = new String[getCSVtoList.size()][getCSVtoList.get(0).size()];
		
		for (int i=0; i<getCSVtoList.size(); i++) {
			for (int j=0; j<getCSVtoList.get(i).size(); j++) {
				getCSVtoArray[i][j] = getCSVtoList.get(i).get(j);				
			}			
		}		
		return getCSVtoArray;		        
	}
	
	
	private static ArrayList<ArrayList<String>> readCSVtoList(String path) throws IOException {
		
		ArrayList<ArrayList<String>> getCSVtoList = new ArrayList<ArrayList<String>>();
		BufferedReader reader = new BufferedReader(new FileReader(path));
		
		//System.out.println(this.path);
		int numline = 0;
		
		while(reader.ready()) {						
			String[] line = reader.readLine().split(",");						
			
			//System.out.println(line);
			ArrayList<String> tmpArr = new ArrayList<String>();
			for( int i=0; i<line.length; i++) {				
				tmpArr.add(line[i]);				
			}
			
			if(numline > 0) getCSVtoList.add(tmpArr);
			numline++;
		}
		reader.close();		
		return getCSVtoList;		
	}
	
	
	public static String[][] doubleToString2D(double[][] array) {
		
		String[][] strArray = new String[array.length][array[0].length];
		
		for(int i=0; i<array.length; i++) {
			for(int j=0; j<array[i].length; j++) {
				strArray[i][j] = String.valueOf(array[i][j]);
			}
		}
		return strArray;
	}
	
	
	public static void writeArraytoCSV(double[][] array, String path) throws IOException {
		writeArraytoCSV(doubleToString2D(array), path);
	}
	
	
	public static void writeArraytoCSV(String[][] array, String path) throws IOException {
		
		BufferedWriter writer    = new BufferedWriter(new FileWriter(new File(path)));
		String         NEWLINE   = System.lineSeparator();
		String         DELIMETER = ",";
	
		for(int i=0; i<array.length; i++) {
			for(int j=0; j<array[i].length; j++) {
				writer.append(array[i][j]);
				if(j< array[i].length-1) writer.append(DELIMETER);			
			}
			writer.write(NEWLINE);
		}			
		writer.close();
	}	
		
	
	//TODO: random number from Linear Congruential Method
	protected static long[] randLinearCongruent(long m, long a, long c, long seed, int count) {
		
		long[] randNum = new long[count];
				
		randNum[0] = (seed * a + c) % m;		
		for(int i=1; i< randNum.length; i++) {			
			randNum[i] = (randNum[i-1] * a + c) % m;			
		}		
		return randNum;
	}
	
	
	protected static double[] randLinearCongruentDbl(long m, long a, long c, long seed, int count) {
				
		long[]   randNum    = randLinearCongruent(m, a, c, seed, count);
		double[] randNumDbl = new double[randNum.length];
		
		for(int i=0; i<randNum.length; i++) {
			randNumDbl[i] = 1.0 * randNum[i] / m;
		}				
		return randNumDbl;
	}
	
	
	protected static double[] randNumInvCdf(double[] randNumDbl) {
		
		double[] randNumInvCdf = new double[randNumDbl.length];
		
		for(int i=0; i<randNumInvCdf.length; i++) {		
			randNumInvCdf[i] = 	new NormalDistribution().inverseCumulativeProbability(randNumDbl[i]);
		}
		return randNumInvCdf;
	}	
	
	
	public static double vectMean(double[] vec) {
		
		double sum = 0.0;		
		for(int i=0; i<vec.length; i++) {
			sum = sum + vec[i];			
		}
		return 1.0 * sum / vec.length;
	}

	
	public static double vectSd(double[] vec) {
		
		double mean = vectMean(vec);		
		double var = 0.0;
		
		for(int i=0; i<vec.length; i++) {
			var = var + Math.pow(vec[i] - mean, 2);				
		}			
		return Math.sqrt(1.0 * var / (vec.length - 1));
	}
	

	public static double vectSdP(double[] vec) {
		
		double mean = vectMean(vec);		
		double var = 0.0;
		
		for(int i=0; i<vec.length; i++) {
			var = var + Math.pow(vec[i] - mean, 2);				
		}			
		return Math.sqrt(1.0 * var / (vec.length));
	}
	
	
	public static double vectSkewP(double [] vec) {
		
		double mean = vectMean(vec);
		double sd   = vectSdP(vec);
		double skew = 0.0;
		
		for(int i=0; i<vec.length; i++) {
			skew = skew + Math.pow(vec[i] - mean, 3);
		}
		return 1.0 * skew / (vec.length) / Math.pow(sd, 3);
	}
	
	
	public static double vectKurtP(double [] vec) {
		
		double mean = vectMean(vec);
		double sd   = vectSdP(vec);		
//		double skew = vectSkewP(vec);
		double kurt = 0.0;		
				
		for(int i=0; i<vec.length; i++) {
			kurt = kurt + Math.pow(vec[i] - mean, 4);
		}
		return 1.0 * kurt / (vec.length) / Math.pow(sd, 4);
	}	
	
	
	public static double vecJBtest(double [] vec) {		
		return 1.0 * (vec.length) / 6.0 * (Math.pow(vectSkewP(vec), 2) + Math.pow(vectKurtP(vec) - 3.0, 2) / 4.0); 
	}	
	
	
	public static double[] vecToZeroMean(double[] vec) {
		
		double[] adjVec = new double[vec.length];
		double mean = vectMean(vec);
		
		for(int i=0; i<vec.length; i++) {
			adjVec[i] = vec[i] - mean;
		}		
		return adjVec;		
	}
	
	
	public static double[] vecToUnitVariance(double[] vec) {
		
		double[] adjVec = new double[vec.length];
		double mean = vectMean(vec);
		double sd   = vectSdP(vec);
		
		for(int i=0; i<vec.length; i++) {
			adjVec[i] = (vec[i] - mean) / sd;
		}		
		return adjVec;		
	}	
	
	
	public static double[][] matTranspose(double[][] mat) {
		
		double[][] matTrans = new double[mat[0].length][mat.length];
		
		for(int i=0; i<mat.length; i++) {
			for(int j=0; j<mat[i].length; j++) {
				matTrans[j][i] = mat[i][j];
			}
		}		
		return matTrans;
	}	
	
	//column wise
	public static double runsTest(double[] vec) {		
		
		double mu = (2.0 * vec.length - 1) / 3.0;
		double sd = Math.sqrt((16.0 * vec.length - 29.0) / 90.0);
		double zStar = (runsCnt(vec) - mu) / sd;
		
//		log.info("{}, {}, {}, {}", mu, sd, zStar, new NormalDistribution().cumulativeProbability(Math.abs(zStar)));		
		
		return 1.0 - new NormalDistribution().cumulativeProbability(Math.abs(zStar));				
	}	
	
	//column wise
	public static double runsCnt(double[] vec) {
		
		String[] upDownStr = new String[vec.length];
		String tempStr = "U";
//		double tempVal = 0.0;
		
		@SuppressWarnings("unused")
		int upCnt = 0;		
		@SuppressWarnings("unused")
		int dnCnt = 0;		
		
		int runCnt = 0;
		
		for(int i=0; i<vec.length-1; i++) {
			if(vec[i+1] > vec[i]) {
//		for(int i=0; i<vec.length; i++) {
//			if(vec[i] > tempVal) {		
				upDownStr[i] = "U";
				upCnt++;
			}
			else {
				upDownStr[i] = "D";
				dnCnt++;
			}
			if(tempStr != upDownStr[i]) runCnt++;
			
//			tempVal = vec[i];
			tempStr = upDownStr[i];				
		}				
//		log.info("{}, {}, {}", upCnt, dnCnt, runCnt);
		
		return runCnt;				
	}		
	
}
