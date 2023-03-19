package com.tzw.eq.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tzw.eq.equtils.Coeff;
import com.tzw.eq.equtils.Filter;
import com.tzw.eq.equtils.FrequencyResponse;
import com.tzw.eq.R;
import com.tzw.eq.view.LineView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int LowShelf = 0;
    private static final int PeakShelf = 1;
    private static final int HighShelf = 2;
    private EditText mEtF;
    private EditText mEtQ;
    private EditText mEtGain;
    private EditText mEtFs;
    private TextView mTvCoeffShow;
    private int mFilterType = LowShelf;
    private LinearLayout mLlChartView;
    private LineView mLineView;

    private Button mBtnComputeCoeffs;

    private boolean 使用默认 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mEtF = findViewById(R.id.et_f);
        mEtQ = findViewById(R.id.et_Q);
        mEtGain = findViewById(R.id.et_gain);
        mEtFs = findViewById(R.id.et_fs);
        mTvCoeffShow = findViewById(R.id.tv_coeff_show);
        mLlChartView = findViewById(R.id.ll_chart_view);
        mLineView = new LineView(MainActivity.this);
        if (使用默认) {
            mLlChartView.addView(mLineView.execute("W", "H"), new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        } else {
            mLlChartView.addView(mLineView.execute2("W", "H"), new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        }
        SeekBar skF = findViewById(R.id.sk_f);
        SeekBar skQ = findViewById(R.id.sk_Q);
        SeekBar skGain = findViewById(R.id.sk_gain);
        SeekBar skPreAmp = findViewById(R.id.sk_pre_amp);

        skF.setMax(20000);
        skQ.setMax(1000);//0.25 - 10
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            skQ.setMin(25);
        }
        skGain.setMax(150);//-15 - +15
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            skGain.setMin(-150);
        }
        skPreAmp.setMax(2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            skPreAmp.setMin(-9);
        }

        skF.setOnSeekBarChangeListener(new SimpleOnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mEtF.setText(i + "");
                doUpdate();
            }
        });

        skQ.setOnSeekBarChangeListener(new SimpleOnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mEtQ.setText(i / 100f + "");
                doUpdate();
            }
        });

        skGain.setOnSeekBarChangeListener(new SimpleOnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mEtGain.setText(i / 10f + "");
                doUpdate();
            }
        });

        skPreAmp.setOnSeekBarChangeListener(new SimpleOnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                ((EditText) findViewById(R.id.et_pre_amp)).setText(i + "");
                doUpdate();
            }
        });

        skF.setProgress(Integer.parseInt(mEtF.getText().toString()));
        skQ.setProgress(Integer.parseInt(mEtQ.getText().toString()));
        skGain.setProgress(Integer.parseInt(mEtGain.getText().toString()));
        skPreAmp.setProgress(Integer.parseInt(((EditText) findViewById(R.id.et_pre_amp)).getText().toString()));
        mEtF.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                skF.setProgress(Integer.parseInt(mEtF.getText().toString()));
            }
        });


        mEtQ.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                skQ.setProgress((int) (Double.parseDouble(mEtQ.getText().toString()) * 100));
            }
        });
        mEtGain.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                skGain.setProgress((int) (Double.parseDouble(mEtGain.getText().toString()) * 10));
            }
        });
        ((EditText) findViewById(R.id.et_pre_amp)).addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                skPreAmp.setProgress(Integer.parseInt(((EditText) findViewById(R.id.et_pre_amp)).getText().toString()));
            }
        });

        Spinner sp = findViewById(R.id.sp);
        final List<String> filters = new ArrayList<>();
        filters.add("LowShelf");
        filters.add("PeakShelf");
        filters.add("HighShelf");
        ArrayAdapter<String> filtersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filters);
        filtersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(filtersAdapter);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                if (filters.get(pos).equals("LowShelf")) {
                    mFilterType = LowShelf;
                }
                if (filters.get(pos).equals("PeakShelf")) {
                    mFilterType = PeakShelf;
                }
                if (filters.get(pos).equals("HighShelf")) {
                    mFilterType = HighShelf;
                }

                doUpdate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mBtnComputeCoeffs = findViewById(R.id.btn_compute_coeffs);
        mBtnComputeCoeffs.setOnClickListener(v -> {
            double[] h = FrequencyResponse.getFreqzn(generateCoeffs(), 100);
            double[] z = FrequencyResponse.getW(100);
            mLineView.updateLine("FreqRes", z, h);
        });

    }

    private Coeff computeCoeff(int type, double f, double q, double gain, int fs) {
        Coeff coeff = null;
        switch (type) {
            case LowShelf:
                coeff = Filter.getLowShelfEQ(f, q, gain, fs);
                break;
            case PeakShelf:
                coeff = Filter.getPeakEQ(f, q, gain, fs);
                break;
            case HighShelf:
                coeff = Filter.getHighShelfEQ(f, q, gain, fs);
                break;
        }
        return coeff;
    }

    private List<Coeff> generateCoeffs() {
        List<Coeff> coeffList = new ArrayList<>();
        Coeff coeff1 = new Coeff();
        coeff1.setB0(0.05634);
        coeff1.setB1(0.05634);
        coeff1.setB2(0);
        coeff1.setA0(1);
        coeff1.setA1(-0.683);
        coeff1.setA2(0);
        coeffList.add(coeff1);
        Coeff coeff2 = new Coeff();
        coeff2.setB0(1); //1 -1.0166 1
        coeff2.setB1(-1.0166);
        coeff2.setB2(1);
        coeff2.setA0(1); //1 -1.4461 0.7957
        coeff2.setA1(-1.4461);
        coeff2.setA2(0.7957);
        coeffList.add(coeff2);
        return coeffList;
    }


    private void doUpdate() {
        if (mEtF.getText().toString().isEmpty() || mEtQ.getText().toString().isEmpty() || mEtGain.getText().toString().isEmpty() || mEtFs.getText().toString().isEmpty()) {
            Toast.makeText(MainActivity.this, "请先输入数据", Toast.LENGTH_SHORT).show();
            return;
        }
        double f = Double.parseDouble(mEtF.getText().toString());
        double q = Double.parseDouble(mEtQ.getText().toString());
        double gain = Double.parseDouble(mEtGain.getText().toString());
        int fs = Integer.parseInt(mEtFs.getText().toString());
        Coeff coeff = computeCoeff(mFilterType, f, q, gain, fs);
        mTvCoeffShow.setText(coeff.toString());
        if (使用默认) {
            double[] h = FrequencyResponse.getFreqzn(coeff, 500);
            double[] z = FrequencyResponse.getW(500);
            mLineView.updateLine("FreqRes", z, h);
        } else {
            List<Coeff> coeffs = new ArrayList<>();
            coeffs.add(coeff);
            visualizeResponse(coeffs, fs);
        }
    }

    private static class SimpleOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    private static class SimpleTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    private void visualizeResponse(List<Coeff> coeffList, int fs) {
        List<Coeff> validCoeffList = new ArrayList<>();
        int n = 200;
        double startF = 20;
        double endF = 20000;
        double logStep = (Math.log10(20000) - Math.log10(20)) / n;
        double[] f = new double[n];
        double step = Math.pow(10, logStep);
        for (int i = 0; i < n; i++) {
            f[i] = startF * Math.pow(step, i); //按对数划分为200个点
        }
        double[] semilogf = new double[n];
        for (int i = 0; i < n; i++) {
            semilogf[i] = Math.log10(f[i]);  //半对数绘制时，对f取以10为底的对数
        }

        List<double[]> xValues = new ArrayList<>();
        List<double[]> yValues = new ArrayList<>();
        List<String> titleList = new ArrayList<>();
        for (int i = 0; i < coeffList.size(); i++) {
            Coeff coeff = coeffList.get(i);
            if (!Double.isNaN(coeff.getB0()) && !Double.isNaN(coeff.getB1()) && !Double.isNaN(coeff.getB2()) && !Double.isNaN(coeff.getA0()) && !Double.isNaN(coeff.getA1()) && !Double.isNaN(coeff.getA2())) {
                validCoeffList.add(coeff);
                double[] h = FrequencyResponse.getFreqzn(coeff, fs, f);
                xValues.add(semilogf);
                yValues.add(h);
                titleList.add("Band" + i);
            }
        }
        if (yValues.size() > 1) {
            double[] overall = FrequencyResponse.getFreqzn(validCoeffList, fs, f);
            xValues.add(semilogf);
            yValues.add(overall);
            titleList.add("Overall");
        }

        mLineView.updateLines(titleList.toArray(new String[1]), xValues, yValues, true); //半对数绘制
    }
}