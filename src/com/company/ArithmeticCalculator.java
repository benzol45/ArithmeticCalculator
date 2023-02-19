package com.company;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//Получить максимальную глубину скобок
//получить выражение внутри максимальной глубины скобок
//внутри выражения найти первую операцию ^ , если нет то * или / , если  нет то - или +
//вычислить результат этой операции
//заменить операцию в строке на результат
//проверить, если в скобках только одно число  - скобки убираем

//TODO Если было что то в двойных и больше скобках после расчёта проверять и удалять ВСЕ
public class ArithmeticCalculator {
    private final String expression;
    private final int round;
    private final boolean logToConsole = true;

    public ArithmeticCalculator(String expression) {
        checkExpression(expression);
        this.expression = prepareExpression(expression);
        this.round = 2;
    }

    public ArithmeticCalculator(String expression, int round) {
        checkExpression(expression);
        this.expression = prepareExpression(expression);
        this.round = round;
    }

    private void checkExpression(String expression) throws IllegalArgumentException {
        // Проверить что там только числа, скобки, возможны пробелы, разделители дробей ., и знаки операций ^*/+-
        // Проверить сбалансированность скобок - открыли столько сколько и закрыли
        // Проверить что не более 1 разделителя дробной части на число (порезать на части знаками операций, убрать всё кроме ., и посмотреть что длинна <=1)

        String incorrectSymbols = expression.replaceAll("[0-9()\\s,.^*/+-]","");
        if (incorrectSymbols.length()>0) {
            throw new IllegalArgumentException("Incorrect symbols: " + incorrectSymbols);
        }

        long openBracketsCount = expression.chars().filter(value -> value=='(').count();
        long closeBracketsCount = expression.chars().filter(value -> value==')').count();
        if (openBracketsCount!=closeBracketsCount) {
            throw new IllegalArgumentException("Incorrect brackets sequence: " + openBracketsCount + " open / " + closeBracketsCount + "close");
        }

        boolean hasMoreOneDelimiter =  Arrays.stream(expression.split("[*^/+-]"))
                .map(s -> s.replaceAll("[^,.]",""))
                .anyMatch(s -> s.length()>1);
        if (hasMoreOneDelimiter) {
            throw new IllegalArgumentException("Incorrect number - must be only ONE fraction delimiter");
        }
    }

    private String prepareExpression(String expression) {
        // Удалить пробелы
        // Заменить все разделители дробей , на .
        // Все отрицательные числа заменить на !n (если перед минусом нет цифры т.е. если там нет ничего, скобка, знак операции)

        expression = expression.replaceAll(" ","");
        expression = expression.replaceAll(",",".");

        List<Integer> listForReplace = new ArrayList<>();
        for (int i = 0; i < expression.length(); i++) {
            if (expression.charAt(i)=='-') {
                if (i==0) {
                    listForReplace.add(i);
                    continue;
                }
                char beforeCurrent = expression.charAt(i-1);
                if (!(beforeCurrent>='0' && beforeCurrent<='9')) {
                    listForReplace.add(i);
                }
            }
        }

        StringBuilder sb = new StringBuilder(expression);
        for (Integer index: listForReplace) {
            sb.setCharAt(index,'!');
        }
        expression = sb.toString();

        return expression;
    }

    public String process() {
        OperationInString fullExpression = new OperationInString(this.expression,-1,-1);

        while (fullExpression.canProcessed()) {

            OperationInString longOperationForProcessing = getMaxLevelExpression(fullExpression.getOperation());
            log("Processed [" + longOperationForProcessing.getStartIndex() + ":" + longOperationForProcessing.getEndIndex() + "] " + longOperationForProcessing.getOperation());

            while (longOperationForProcessing.canProcessed()) {
                OperationInString operation = getFirstOperation(longOperationForProcessing.getOperation());
                log("Calculate [" + operation.getStartIndex() + ":" + operation.getEndIndex() + "] " + operation.getOperation());
                String result = processOperation(operation.getOperation());
                log("= " + result);

                longOperationForProcessing.setResult(result, operation.getStartIndex(), operation.getEndIndex());
                log("New operation: " + longOperationForProcessing.getOperation());
            }

            fullExpression.setResult(longOperationForProcessing.getOperation(), longOperationForProcessing.getStartIndex(), longOperationForProcessing.getEndIndex());
            log(fullExpression.getOperation());
        }

        String result = fullExpression.getOperation();
        result = result.replaceAll("!","-");
        return result;
    }

    private void log(String string) {
        if (logToConsole) {
            System.out.println(string);
        }
    }

    static class OperationInString {
        private String operation;
        private int startIndex;
        private int endIndex;

        public OperationInString(String operation, int startIndex, int endIndex) {
            this.operation = operation;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        public String getOperation() {
            return operation;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        public void setResult(String result, int from, int to) {
            //If we have brackets on the left and right by result we must remove these brackets
            //TODO or we can have more than one brackets. We must remove all of them
            if (from-1>0 && to+1<operation.length() && operation.charAt(from-1)=='(' && operation.charAt(to+1)==')') {
                from--;
                to++;
            }

            String newOperation = operation.substring(0,from) + result + operation.substring(to+1);

            operation = newOperation;
        }

        public boolean canProcessed() {
            return operation.contains("^") || operation.contains("*") || operation.contains("/") || operation.contains("+") || operation.contains("-");
        }
    }

    protected int getMaxLevelOfBrackets(String stringForAnalyse) {
        List<Integer> listBrackets = stringForAnalyse.chars()
                .boxed()
                .filter(value -> (value=='(') || (value==')'))
                .collect(Collectors.toList());

        if (listBrackets.size()==0) {
            return 0;
        }

        int max = 0;
        int counter = 0;
        for (Integer value: listBrackets) {
            if (value=='(') {
                counter++;
                max = Math.max(max,counter);
            } else if (value==')') {
                counter--;
            }
        }

        return max;
    }

    protected OperationInString getMaxLevelExpression(String stringForAnalyse) {
        int maxLevelBrackets = getMaxLevelOfBrackets(stringForAnalyse);
        if (maxLevelBrackets==0) {
            return new OperationInString(stringForAnalyse,0, stringForAnalyse.length()-1);
        }

        int startIndex = -1;
        int finishIndex = -1;
        int counter = 0;
        boolean inExpression = false;
        for (int i =0; i<stringForAnalyse.length(); i++) {
            if (stringForAnalyse.charAt(i)=='(') {
                counter++;
                if (counter==maxLevelBrackets) {
                    startIndex = i+1;
                    inExpression = true;
                }
            } else if (stringForAnalyse.charAt(i)==')') {
                if (inExpression) {
                    finishIndex = i;
                    break;
                }

                counter--;
            }
        }

        return new OperationInString(stringForAnalyse.substring(startIndex, finishIndex),startIndex,finishIndex-1);
    }

    protected OperationInString getFirstOperation(String stringForAnalyse) {
        if (stringForAnalyse.contains("^")) {
            //^
            int operationIndex = stringForAnalyse.indexOf("^");

            return getOperationAroundIndex(stringForAnalyse, operationIndex);
        } else if (stringForAnalyse.contains("*") || stringForAnalyse.contains("/")) {
            //*/
            int operationMIndex = stringForAnalyse.indexOf("*");
            operationMIndex = (operationMIndex==-1) ? Integer.MAX_VALUE : operationMIndex;
            int operationDIndex = stringForAnalyse.indexOf("/");
            operationDIndex = (operationDIndex==-1) ? Integer.MAX_VALUE : operationDIndex;
            int operationIndex = Math.min(operationMIndex,operationDIndex);

            return getOperationAroundIndex(stringForAnalyse, operationIndex);
        } else {
            //+-
            int operationAIndex = stringForAnalyse.indexOf("+");
            operationAIndex = (operationAIndex==-1) ? Integer.MAX_VALUE : operationAIndex;
            int operationSIndex = stringForAnalyse.indexOf("-");
            operationSIndex = (operationSIndex==-1) ? Integer.MAX_VALUE : operationSIndex;
            int operationIndex = Math.min(operationAIndex,operationSIndex);

            return getOperationAroundIndex(stringForAnalyse, operationIndex);
        }
    }

    private OperationInString getOperationAroundIndex(String stringForAnalyse, int operationIndex) {
        int startIndex = 0;
        for (int i = operationIndex -1; i >=0 ; i--) {
            char currentChar = stringForAnalyse.charAt(i);
            if (currentChar=='^' || currentChar=='*' || currentChar=='/' || currentChar=='+' || currentChar=='-') {
                startIndex = i+1;
                break;
            }
        }

        int finishIndex = stringForAnalyse.length();
        for (int i = operationIndex +1; i < stringForAnalyse.length() ; i++) {
            char currentChar = stringForAnalyse.charAt(i);
            if (currentChar=='^' || currentChar=='*' || currentChar=='/' || currentChar=='+' || currentChar=='-') {
                finishIndex = i;
                break;
            }
        }

        return new OperationInString(stringForAnalyse.substring(startIndex,finishIndex),startIndex,finishIndex-1);
    }

    protected String processOperation(String operation) {
        String[] parts = operation.split("[\\^*/+\\-]");
        double first = stringToValue(parts[0]);
        double second = stringToValue(parts[1]);

        if (operation.contains("^")) {
            return valueToString(Math.pow(first,second));
        } else if (operation.contains("*")) {
            return valueToString(first * second);
        } else if (operation.contains("/")) {
            return valueToString(first / second);
        } else if (operation.contains("+")) {
            return valueToString(first + second);
        } else if (operation.contains("-")) {
            return valueToString(first - second);
        }

        throw new IllegalArgumentException("Unsupported expression " + operation);
    }

    private double stringToValue(String string) {

        int k = 1;
        if (string.startsWith("!")) {
            string = string.substring(1);
            k = -1;
        }

        return Double.parseDouble(string) * k;
    }

    private String valueToString(double value) {
        String prefix = (value<0) ? "!" : "";
        value = Math.abs(value);

        int k = (int)Math.pow(10,round);
        double roundedValue = ((double)Math.round(value*k))/k;
        String roundedValueAsString;
        if (roundedValue==(int)roundedValue) {
            roundedValueAsString = Integer.toString((int)roundedValue);
        } else {
            roundedValueAsString = Double.toString(roundedValue);
        }

        return prefix + roundedValueAsString;
    }
}
