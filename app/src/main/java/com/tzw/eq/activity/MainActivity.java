package com.tzw.eq.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mEtF = (EditText) findViewById(R.id.et_f);
        mEtQ = (EditText) findViewById(R.id.et_Q);
        mEtGain = (EditText) findViewById(R.id.et_gain);
        mEtFs = (EditText) findViewById(R.id.et_fs);
        mTvCoeffShow = (TextView) findViewById(R.id.tv_coeff_show);
        mLlChartView = (LinearLayout) findViewById(R.id.ll_chart_view);
        mLineView = new LineView(MainActivity.this);
        mLlChartView.addView(mLineView.execute("W", "H"), new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));

        SeekBar skF = findViewById(R.id.sk_f);
        SeekBar skQ = findViewById(R.id.sk_Q);
        SeekBar skGain = findViewById(R.id.sk_gain);
        SeekBar skPreAmp = findViewById(R.id.sk_pre_amp);

        skF.setMax(20000);
        skQ.setMax(10);
        skGain.setMax(15);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            skGain.setMin(-15);
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
                mEtQ.setText(i + "");
                doUpdate();
            }
        });

        skGain.setOnSeekBarChangeListener(new SimpleOnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mEtGain.setText(i + "");
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
        double[] h = FrequencyResponse.getFreqzn(coeff, 500);
        double[] z = FrequencyResponse.getW(500);
        mLineView.updateLine("FreqRes", z, h);
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
}