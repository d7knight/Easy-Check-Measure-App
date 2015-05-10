package com.example.david.distoapp;

import java.util.HashSet;

/**
 * Created by David on 4/23/2015.
 */
public class FractionNum {

    int base=0;
    int numerator=0;
    int denominator=1;
    private double fullnumber=0;
    private double fractionpart=0;
    private static final int precision=8;
    private static final double METER2FEET = 3.28083989501312;
    private static final double METER2INCH = METER2FEET*12;

    public double getFractionpart() {
        return fractionpart;
    }

    public void setFractionpart(double fractionpart) {
        this.fractionpart = fractionpart;
    }

    public double getFullnumber() {
        return fullnumber;
    }

    public void setFullnumber(double fullnumber) {
        this.fullnumber = fullnumber;
    }

    public int getDenominator() {
        return denominator;
    }

    public void setDenominator(int denominator) {
        this.denominator = denominator;
    }

    public int getNumerator() {
        return numerator;
    }

    public void setNumerator(int numerator) {
        this.numerator = numerator;
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }


    private final HashSet<Integer> denoms = new HashSet<Integer>() {{
        add(1);
        add(2);
        add(4);
        add(8);
        add(16);
        add(32);
    }};

    private int gcd(int a, int b)
    {
        if (a == 0) return b;
        if (b == 0) return a;

        if (a > b)
            return gcd(b, a % b);
        else
            return gcd(a, b % a);
    }
    public FractionNum(String s){
        String[] arr=s.split(" ");
        base=Integer.parseInt(arr[0]);
        if(arr.length>1) {
            String fraction=arr[1];
            String[] parts=fraction.split("/");
            if(parts.length==2){
                numerator=Integer.parseInt(parts[0]);
                denominator=Integer.parseInt(parts[1]);
            }
        }
        fractionpart=(double)numerator/(double)denominator;
        fullnumber=(double)base + fractionpart;
    }


     public FractionNum(double meters){
        fullnumber=meters*METER2INCH;
        base=(int)fullnumber;
        fractionpart=fullnumber-(double)base;
        if(fractionpart!=0) {
            int shiftfactor = 1;
            for (int i = 0; i < precision; i++) {
                shiftfactor *= 10;
            }

            double num = ((double) fractionpart * (double) shiftfactor);
            int gcdiv = gcd((int) num, shiftfactor);


            this.denominator = shiftfactor / gcdiv;
            //had issues with the number being 1/32" to small which is why I rounded up
            this.numerator = (int) ((((double) num) / ((double) gcdiv) * 32) / ((double) this.denominator) + .5);
            this.denominator = 32;

            gcdiv = gcd(numerator, denominator);
            if (gcdiv != 0) {
                this.numerator /= gcdiv;
                this.denominator /= gcdiv;
            }

            if (this.denominator == this.numerator && this.denominator != 0) {
                this.numerator = 0;
                this.denominator = 0;
                this.base++;
            }
        }




    }
    public String str(){
        if(numerator!=0) {
            return Integer.toString(base) + " " + Integer.toString(numerator) + "/" + Integer.toString(denominator);
        }
        else{
            return Integer.toString(base);
        }

    }


}
