import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

enum TokenType {
    NUMBER, OPERATOR, FUNCTION, CONSTANT, LEFT_PAREN, RIGHT_PAREN, COMMA
}

class Token {
    TokenType type;
    String value;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }
}

public class ScientificCalculator extends JFrame {
    private JTextField display;
    private String input = "";
    private boolean isDegrees = true;

    public ScientificCalculator() {
        setTitle("Scientific Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 800);
        setLayout(new BorderLayout());

        display = new JTextField();
        display.setEditable(false);
        display.setFont(new Font("Arial", Font.PLAIN, 24));
        add(display, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(6, 5, 5, 5));

        String[] buttonLabels = {
            "AC", "C", "Deg/Rad", "π", "e",
            "sin", "cos", "tan", "asin", "acos",
            "atan", "log", "ln", "sqrt", "root",
            "7", "8", "9", "(", ")",
            "4", "5", "6", "*", "/",
            "1", "2", "3", "+", "-",
            "0", ".", "^", "fact", "=",
            ",", "10^x", "e^x"
        };

        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.addActionListener(new ButtonClickListener());
            buttonPanel.add(button);
        }

        add(buttonPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private class ButtonClickListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            switch (command) {
                case "=":
                    calculateResult();
                    break;
                case "AC":
                    input = "";
                    display.setText("");
                    break;
                case "C":
                    if (!input.isEmpty()) {
                        input = input.substring(0, input.length() - 1);
                        display.setText(input);
                    }
                    break;
                case "Deg/Rad":
                    isDegrees = !isDegrees;
                    break;
                case "π":
                    input += "π";
                    display.setText(input);
                    break;
                case "e":
                    input += "e";
                    display.setText(input);
                    break;
                case "10^x":
                    input += "10^(";
                    display.setText(input);
                    break;
                case "e^x":
                    input += "e^(";
                    display.setText(input);
                    break;
                case "sin":
                case "cos":
                case "tan":
                case "asin":
                case "acos":
                case "atan":
                case "log":
                case "ln":
                case "sqrt":
                case "root":
                case "fact":
                    input += command + "(";
                    display.setText(input);
                    break;
                default:
                    if (command.matches("[0-9]|\\+|-|\\*|/|\\^|\\(|\\)|,|\\.|π|e")) {
                        input += command;
                        display.setText(input);
                    }
            }
        }
    }

    private void calculateResult() {
        try {
            List<Token> tokens = tokenize(input);
            List<Token> postfix = shuntingYard(tokens);
            double result = evaluatePostfix(postfix, isDegrees);
            display.setText(String.format("%.6f", result));
            input = String.valueOf(result);
        } catch (Exception ex) {
            display.setText("Error: " + ex.getMessage());
            input = "";
        }
    }

    private List<Token> tokenize(String input) throws Exception {
        List<Token> tokens = new ArrayList<>();
        int pos = 0;
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (Character.isDigit(c) || c == '.') {
                StringBuilder sb = new StringBuilder();
                boolean hasDecimal = false;
                while (pos < input.length() && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
                    if (input.charAt(pos) == '.') {
                        if (hasDecimal) throw new Exception("Invalid number");
                        hasDecimal = true;
                    }
                    sb.append(input.charAt(pos));
                    pos++;
                }
                tokens.add(new Token(TokenType.NUMBER, sb.toString()));
            } else if (Character.isLetter(c)) {
                StringBuilder sb = new StringBuilder();
                while (pos < input.length() && Character.isLetter(input.charAt(pos))) {
                    sb.append(input.charAt(pos));
                    pos++;
                }
                String str = sb.toString().toLowerCase();
                switch (str) {
                    case "pi":
                        tokens.add(new Token(TokenType.CONSTANT, "π"));
                        break;
                    case "e":
                        tokens.add(new Token(TokenType.CONSTANT, "e"));
                        break;
                    case "sin":
                    case "cos":
                    case "tan":
                    case "asin":
                    case "acos":
                    case "atan":
                    case "log":
                    case "ln":
                    case "sqrt":
                    case "root":
                    case "fact":
                        tokens.add(new Token(TokenType.FUNCTION, str));
                        break;
                    default:
                        throw new Exception("Unknown identifier: " + str);
                }
            } else if (c == '+' || c == '-' || c == '*' || c == '/' || c == '^') {
                tokens.add(new Token(TokenType.OPERATOR, String.valueOf(c)));
                pos++;
            } else if (c == '(') {
                tokens.add(new Token(TokenType.LEFT_PAREN, "("));
                pos++;
            } else if (c == ')') {
                tokens.add(new Token(TokenType.RIGHT_PAREN, ")"));
                pos++;
            } else if (c == ',') {
                tokens.add(new Token(TokenType.COMMA, ","));
                pos++;
            } else if (c == 'π' || c == 'e') {
                tokens.add(new Token(TokenType.CONSTANT, String.valueOf(c)));
                pos++;
            } else {
                throw new Exception("Invalid character: " + c);
            }
        }
        return tokens;
    }

    private List<Token> shuntingYard(List<Token> tokens) throws Exception {
        List<Token> output = new ArrayList<>();
        Stack<Token> stack = new Stack<>();

        for (Token token : tokens) {
            switch (token.type) {
                case NUMBER:
                case CONSTANT:
                    output.add(token);
                    break;
                case FUNCTION:
                    stack.push(token);
                    break;
                case LEFT_PAREN:
                    stack.push(token);
                    break;
                case RIGHT_PAREN:
                    while (!stack.isEmpty() && stack.peek().type != TokenType.LEFT_PAREN) {
                        output.add(stack.pop());
                    }
                    if (stack.isEmpty()) {
                        throw new Exception("Mismatched parentheses");
                    }
                    stack.pop();
                    if (!stack.isEmpty() && stack.peek().type == TokenType.FUNCTION) {
                        output.add(stack.pop());
                    }
                    break;
                case COMMA:
                    while (!stack.isEmpty() && stack.peek().type != TokenType.LEFT_PAREN) {
                        output.add(stack.pop());
                    }
                    if (stack.isEmpty()) {
                        throw new Exception("Mismatched parentheses or comma");
                    }
                    break;
                case OPERATOR:
                    while (!stack.isEmpty() &&
                           (stack.peek().type == TokenType.OPERATOR || stack.peek().type == TokenType.FUNCTION) &&
                           (getPrecedence(token) <= getPrecedence(stack.peek()) && !isRightAssociative(token))) {
                        output.add(stack.pop());
                    }
                    stack.push(token);
                    break;
                default:
                    throw new Exception("Invalid token type: " + token.type);
            }
        }

        while (!stack.isEmpty()) {
            Token token = stack.pop();
            if (token.type == TokenType.LEFT_PAREN || token.type == TokenType.RIGHT_PAREN) {
                throw new Exception("Mismatched parentheses");
            }
            output.add(token);
        }

        return output;
    }

    private int getPrecedence(Token token) {
        if (token.type == TokenType.FUNCTION) {
            return 5;
        } else if (token.type == TokenType.OPERATOR) {
            String op = token.value;
            switch (op) {
                case "^":
                    return 4;
                case "*":
                case "/":
                    return 3;
                case "+":
                case "-":
                    return 2;
                default:
                    return 0;
            }
        }
        return 0;
    }

    private boolean isRightAssociative(Token token) {
        return token.type == TokenType.OPERATOR && token.value.equals("^");
    }

    private double evaluatePostfix(List<Token> postfix, boolean isDegrees) throws Exception {
        Stack<Double> stack = new Stack<>();

        for (Token token : postfix) {
            switch (token.type) {
                case NUMBER:
                    stack.push(Double.parseDouble(token.value));
                    break;
                case CONSTANT:
                    if (token.value.equals("π")) {
                        stack.push(Math.PI);
                    } else if (token.value.equals("e")) {
                        stack.push(Math.E);
                    } else {
                        throw new Exception("Unknown constant: " + token.value);
                    }
                    break;
                case OPERATOR:
                    if (stack.size() < 2) {
                        throw new Exception("Insufficient operands for operator " + token.value);
                    }
                    double b = stack.pop();
                    double a = stack.pop();
                    switch (token.value) {
                        case "+":
                            stack.push(a + b);
                            break;
                        case "-":
                            stack.push(a - b);
                            break;
                        case "*":
                            stack.push(a * b);
                            break;
                        case "/":
                            if (b == 0) throw new Exception("Division by zero");
                            stack.push(a / b);
                            break;
                        case "^":
                            stack.push(Math.pow(a, b));
                            break;
                        default:
                            throw new Exception("Unknown operator: " + token.value);
                    }
                    break;
                case FUNCTION:
                    String func = token.value;
                    int arity;
                    switch (func) {
                        case "sin":
                        case "cos":
                        case "tan":
                        case "asin":
                        case "acos":
                        case "atan":
                        case "log":
                        case "ln":
                        case "sqrt":
                        case "fact":
                            arity = 1;
                            break;
                        case "root":
                            arity = 2;
                            break;
                        default:
                            throw new Exception("Unknown function: " + func);
                    }
                    if (stack.size() < arity) {
                        throw new Exception("Insufficient arguments for function " + func);
                    }
                    double[] args = new double[arity];
                    for (int i = arity - 1; i >= 0; i--) {
                        args[i] = stack.pop();
                    }
                    double result = evaluateFunction(func, args, isDegrees);
                    stack.push(result);
                    break;
                default:
                    throw new Exception("Unexpected token type: " + token.type);
            }
        }

        if (stack.size() != 1) {
            throw new Exception("Invalid expression");
        }
        return stack.pop();
    }

    private double evaluateFunction(String func, double[] args, boolean isDegrees) throws Exception {
        switch (func) {
            case "sin":
                return isDegrees ? Math.sin(Math.toRadians(args[0])) : Math.sin(args[0]);
            case "cos":
                return isDegrees ? Math.cos(Math.toRadians(args[0])) : Math.cos(args[0]);
            case "tan":
                return isDegrees ? Math.tan(Math.toRadians(args[0])) : Math.tan(args[0]);
            case "asin":
                return isDegrees ? Math.toDegrees(Math.asin(args[0])) : Math.asin(args[0]);
            case "acos":
                return isDegrees ? Math.toDegrees(Math.acos(args[0])) : Math.acos(args[0]);
            case "atan":
                return isDegrees ? Math.toDegrees(Math.atan(args[0])) : Math.atan(args[0]);
            case "log":
                return Math.log10(args[0]);
            case "ln":
                return Math.log(args[0]);
            case "sqrt":
                return Math.sqrt(args[0]);
            case "root":
                return Math.pow(args[1], 1.0 / args[0]);
            case "fact":
                int n = (int) args[0];
                if (n < 0) throw new Exception("Factorial of negative number");
                double fact = 1;
                for (int i = 2; i <= n; i++) {
                    fact *= i;
                }
                return fact;
            default:
                throw new Exception("Unknown function: " + func);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ScientificCalculator::new);
    }
}