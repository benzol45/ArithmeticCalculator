package com.company;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArithmeticCalculatorTest {
    private ArithmeticCalculator arithmeticCalculator = new ArithmeticCalculator("0");

    @Test
    void getMaxLevelOfBrackets() {
        assertEquals(0, arithmeticCalculator.getMaxLevelOfBrackets("1+1"));
        assertEquals(1, arithmeticCalculator.getMaxLevelOfBrackets("(1+1)"));
        assertEquals(3, arithmeticCalculator.getMaxLevelOfBrackets("(((1.1^1+1.1)))"));
        assertEquals(3, arithmeticCalculator.getMaxLevelOfBrackets("(1.1+(1.2+(1.3+1.4)))"));
        assertEquals(3, arithmeticCalculator.getMaxLevelOfBrackets("(1+(1+1))+(!1+(1+(!1.5^2+1)))"));
        assertEquals(3, arithmeticCalculator.getMaxLevelOfBrackets("(1+(1+1))+(1+(!1+1^1)*(1+(1+1))+(!1+1))"));
    }

    @Test
    void getMaxLevelExpression() {
        assertEquals("1+1", arithmeticCalculator.getMaxLevelExpression("1+1").getOperation());
        assertEquals("1+1.1^1", arithmeticCalculator.getMaxLevelExpression("(1+1.1^1)").getOperation());
        assertEquals("1+!1.2", arithmeticCalculator.getMaxLevelExpression("(1+!1.2)").getOperation());
        assertEquals("1+1.1", arithmeticCalculator.getMaxLevelExpression("(((1+1.1)))").getOperation());
        assertEquals("3.1^5+4", arithmeticCalculator.getMaxLevelExpression("(1+(2+(3.1^5+4)))").getOperation());
        assertEquals("6.1+7*!8.2-9", arithmeticCalculator.getMaxLevelExpression("(1+(2+3))+(4+(5+(6.1+7*!8.2-9)))").getOperation());
        assertEquals("!8.0*8^8+!9.99^10", arithmeticCalculator.getMaxLevelExpression("(1+(2+3))+(4+(5+6)*(7+(!8.0*8^8+!9.99^10))+(0+1))").getOperation());
    }

    @Test
    void getFirstOperation() {
        //Exponentiation
        assertEquals("1^1", arithmeticCalculator.getFirstOperation("1^1").getOperation());
        assertEquals("1^1", arithmeticCalculator.getFirstOperation("1^1*2-3+4").getOperation());
        assertEquals("2^2", arithmeticCalculator.getFirstOperation("1*2^2-3+4").getOperation());
        assertEquals("!3^3", arithmeticCalculator.getFirstOperation("1*2-!3^3+4").getOperation());
        assertEquals("!3^3", arithmeticCalculator.getFirstOperation("1*2-!3^3+4^4").getOperation());
        //Multiplication and Division
        assertEquals("1*1", arithmeticCalculator.getFirstOperation("1*1").getOperation());
        assertEquals("1*2", arithmeticCalculator.getFirstOperation("1*2-3+4").getOperation());
        assertEquals("1*!2", arithmeticCalculator.getFirstOperation("1*!2-3+4").getOperation());
        assertEquals("1/2", arithmeticCalculator.getFirstOperation("0-1/2-3+4").getOperation());
        assertEquals("!1/!2", arithmeticCalculator.getFirstOperation("0-!1/!2-3+4").getOperation());
        assertEquals("1/2", arithmeticCalculator.getFirstOperation("0+1/2*3+4").getOperation());
        assertEquals("!1/!2", arithmeticCalculator.getFirstOperation("!0+!1/!2*!3+!4").getOperation());
        //Addition and Subtraction
        assertEquals("1-1", arithmeticCalculator.getFirstOperation("1-1").getOperation());
        assertEquals("1+2", arithmeticCalculator.getFirstOperation("1+2-3+4").getOperation());
        assertEquals("!1+2", arithmeticCalculator.getFirstOperation("!1+2-3+4").getOperation());
        assertEquals("1+!2", arithmeticCalculator.getFirstOperation("1+!2-3+4").getOperation());
    }

    @Test
    void processOperation() {
        assertEquals("2", arithmeticCalculator.processOperation("1+1"));
        assertEquals("2.3",arithmeticCalculator.processOperation("1.1+1.2"));
        assertEquals("!0.1",arithmeticCalculator.processOperation("1.1-1.2"));
        assertEquals("!1.32",arithmeticCalculator.processOperation("1.1*!1.2"));
        assertEquals("0.1",arithmeticCalculator.processOperation("!1.1+1.2"));
        assertEquals("!3.33",arithmeticCalculator.processOperation("!1/0.3"));
        assertEquals("1", arithmeticCalculator.processOperation("1^1"));
        assertEquals("0", arithmeticCalculator.processOperation("0^100"));
        assertEquals("1", arithmeticCalculator.processOperation("!999^0"));
        assertEquals("4", arithmeticCalculator.processOperation("!2^2"));
        assertEquals("!8", arithmeticCalculator.processOperation("!2^3"));
    }

    @Test
    void process() {
        ArithmeticCalculator arithmeticCalculator;
        arithmeticCalculator = new ArithmeticCalculator("-1^2+(2+(3^2+4+5))");
        assertEquals("21", arithmeticCalculator.process());
        arithmeticCalculator = new ArithmeticCalculator("-1^2/(2+(3^2+4+5))");
        assertEquals("0.05", arithmeticCalculator.process());
        arithmeticCalculator = new ArithmeticCalculator("-1^3/(2+(3^2+4+5))");
        assertEquals("-0.05", arithmeticCalculator.process());
        arithmeticCalculator = new ArithmeticCalculator("-1.2 / 0,6");
        assertEquals("-2", arithmeticCalculator.process());
        arithmeticCalculator = new ArithmeticCalculator("-1.5 / 0,6");
        assertEquals("-2.5", arithmeticCalculator.process());
    }

    @Test
    void processIncorrectParameters() {
        assertThrows(IllegalArgumentException.class, ()->new ArithmeticCalculator("1+2-three*4"));
        assertThrows(IllegalArgumentException.class, ()->new ArithmeticCalculator("1.1+(2-(3*4)"));
        assertThrows(IllegalArgumentException.class, ()->new ArithmeticCalculator("1.1+(2.2.2-(3.3*4))"));
    }
}