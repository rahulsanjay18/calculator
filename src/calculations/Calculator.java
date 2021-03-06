package calculations;



import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calculator {

	private static String expression;

	private static int parenIndexes[];

	private static String parenString = "";

	private static String regex = "[\\+\\−×/^√]";// Uses − instead of - (−/-) and × instead of x (×/x)

	private static boolean isitRadians;
	
	private final static String[] FUNCTIONS = {"sin","cos","tan","csc","sec","cot"};

	private final static int SCALE_SET = 16;

	private final static double PI = 3.1415926535897932384626433832795028841971693993751;
	
	// main method to set up expression to calculate. Expression must be all surrounded in parenthesis.
	public static String getAnswer(String input, boolean rad) {
		// used for a placeholder for radian to degree conversions
		rad = false;
		isitRadians = rad;

		expression = "(" + input + ")";
		expression = toCalculatable(expression);
		paren();
		
		// rounds answer up to avoid repeating 9's
		if(!expression.contains("[^0-9]"))
			expression = numberToString((new BigDecimal(expression).setScale(SCALE_SET-1, BigDecimal.ROUND_HALF_UP)));
		
		// displays answer
		return expression;

	}
	
	// converts string to something the calculator can calculate
	private static String toCalculatable(String toConvert){
		toConvert = toConvert.replaceAll("\\)\\(", "\\)×\\(");

		Pattern p = Pattern.compile("[0-9][\\(a-z]");
		Matcher m = p.matcher(toConvert);

		while(m.find()) {
			String found = toConvert.substring(m.start(), m.end());

			if(found.contains("("))
				found = found.substring(0, found.indexOf("(")) + "\\" + found.substring(found.indexOf("("));

			toConvert = toConvert.replaceAll(found, found.charAt(0) + "×" + found.charAt(found.length() - 1));
		}


		return toConvert;
	}

	// evaluates each parenthetical expression first
	private static void paren() {

		expression = expression.replaceAll(" ", "");

		while (expression.contains("(")){
			int numParen = expression.length() - expression.replaceAll("[()]", "").length();

			parenIndexes = new int[numParen];
			int j = 0;
			for(int i = 0; i < expression.length(); i++){
				if(expression.charAt(i) == '(' || expression.charAt(i) == ')'){
					parenIndexes[j] = i;
					j++;
				}
			}

			parenString = expression.replaceAll("[^()]", "");

			int parenDex = parenString.indexOf("()");

			expression = eval(expression.substring(parenIndexes[parenDex] + 1, parenIndexes[parenDex + 1]));

			parenString = parenString.replace("()", "");
		}
	}

	private static String eval(String equation) {

		equation = equation.replaceAll("", "");
		String parts[] = createArrayParts(equation);
		String origEquation = equation;

		for(String func : FUNCTIONS){

			while(equation.contains(func)){
				int funcIndex = findIndexOf(parts, func);

				String part = parts[funcIndex];

				BigDecimal number1 = new BigDecimal(part.substring(func.length()));

				BigDecimal numberAns = functionEval(number1, func);

				equation = equation.replace(func + numberToString(number1), numberToString(numberAns));

				parts = createArrayParts(equation);

				System.out.println(equation);
			}

		}

		while (equation.indexOf('^') != -1 || equation.indexOf('√') != -1) {
			int expIndex = findIndexOf(parts, "^");
			int radIndex = findIndexOf(parts, "√");
			BigDecimal number3;

			if (radIndex < 0 ||(expIndex > 0 && expIndex < radIndex)) {
				BigDecimal number1 = BigDecimal.valueOf(Double.parseDouble(parts[expIndex - 1]));
				int number2 = Integer.parseInt(parts[expIndex + 1]);

				number3 = number1.pow(number2);

				equation = equation.replace(numberToString(number1) +  "^"  + number2, numberToString(number3));
			}else {

				BigDecimal number1 = BigDecimal.valueOf(Double.parseDouble(parts[radIndex + 1]));

				number3 = sqrt(number1);

				equation = equation.replace("√" + numberToString(number1), numberToString(number3));

			}
			parts = createArrayParts(equation);
		}

		while (equation.indexOf('/') != -1 || equation.indexOf('×') != -1) {

			int divIndex = findIndexOf(parts, "/");
			int multIndex = findIndexOf(parts, "×");
			BigDecimal number3 = BigDecimal.ZERO;
			if(multIndex < 0 ||(divIndex > 0 && divIndex < multIndex)){
				BigDecimal number1 = BigDecimal.valueOf(Double.parseDouble(parts[divIndex - 1]));
				BigDecimal number2 = BigDecimal.valueOf(Double.parseDouble(parts[divIndex + 1]));

				if(number2.compareTo(BigDecimal.ZERO) == 0){
					equation = "undefined";
					expression = "undefined";
				}else{
					number3 = BigDecimal.valueOf(number1.doubleValue() / number2.doubleValue());
				}
				equation = equation.replace(numberToString(number1) + "/" + numberToString(number2), numberToString(number3));

			}else{
				BigDecimal number1 = BigDecimal.valueOf(Double.parseDouble(parts[multIndex - 1]));
				BigDecimal number2 = BigDecimal.valueOf(Double.parseDouble(parts[multIndex + 1]));
				number3 = number1.multiply(number2);

				equation = equation.replace(numberToString(number1) + "×" + numberToString(number2), numberToString(number3));
			}

			parts = createArrayParts(equation);
		}

		while (equation.indexOf('+') != -1 || equation.indexOf('−') != -1) {
			int addIndex = findIndexOf(parts, "+");
			int subIndex = findIndexOf(parts, "−");
			BigDecimal number3;
			if(subIndex < 0 || (addIndex > 0 && addIndex < subIndex)){
			BigDecimal number1 = BigDecimal.valueOf(Double.parseDouble(parts[addIndex - 1]));
			BigDecimal number2 = BigDecimal.valueOf(Double.parseDouble(parts[addIndex + 1]));
			number3 = number1.add(number2);
			equation = equation.replace(numberToString(number1) + "+" + numberToString(number2), numberToString(number3));

			}else{
				BigDecimal number1 = BigDecimal.valueOf(Double.parseDouble(parts[subIndex - 1]));
				BigDecimal number2 = BigDecimal.valueOf(Double.parseDouble(parts[subIndex + 1]));
				number3 = number1.subtract(number2);

				equation = equation.replace(numberToString(number1) + "−" + numberToString(number2), numberToString(number3));

			}	
			parts = createArrayParts(equation);

		}
		return expression.replace("("+ origEquation + ")", "" + equation);
	}

	private static int findIndexOf(String  parts[], String part) {
		if (part.length() == 1) {
			for (int i = 0; i < parts.length; i++) {
				if (parts[i].equals(part)) {
					return i;
				}
			} 
		}else{
			for (int i = 0; i < parts.length; i++) {
				if (parts[i].contains(part)) {
					return i;
				}
			}
		}
		return -1;
	}

	private static String[] createArrayParts(String equation) {
		String part = "";
		String equationParts[] = new String[equation.replaceAll(" ", "").replaceAll(regex, "` `").split("`").length];
		int j = 0;
		for (int i = 0; i < equation.length(); i++){
			String singleChar = "" + equation.charAt(i);

			if(singleChar.matches(regex)){
				equationParts[j] = part;
				j++;
				equationParts[j] = singleChar;
				j++;
				part = "";
			}else{
				part += singleChar;
			}
		}
		equationParts[j] = part;
		return equationParts;
	}

	private static BigDecimal functionEval(BigDecimal number, String func) {
		double answer = 0;
		double angle = number.doubleValue();
		// for when degree conversion is implemented
		if(!isitRadians)
			angle = angle * PI / 180;
		
		
		try {
			switch (func) {
			case "sin":
				answer = Math.sin(angle);
				break;
			case "cos":
				answer = Math.cos(angle);
				break;
			case "tan":
				answer = Math.tan(angle);
				break;
			case "csc":
				answer = 1 / Math.sin(angle);
				break;
			case "sec":
				answer = 1 / Math.cos(angle);
				break;
			case "cot":
				answer = 1 / Math.tan(angle);
				break;
			default:
				break;
			}
			return BigDecimal.valueOf(answer).setScale(SCALE_SET, BigDecimal.ROUND_HALF_UP);
		} catch (Exception e) {
			expression = "undef";
		}
		return BigDecimal.ZERO;
	}

	private static BigDecimal sqrt(BigDecimal num) {
		return BigDecimal.valueOf(Math.sqrt(num.doubleValue()));
	}

	private static String numberToString(BigDecimal number) {
		return number.setScale(SCALE_SET, BigDecimal.ROUND_FLOOR).stripTrailingZeros().toPlainString().trim();
	}
}
