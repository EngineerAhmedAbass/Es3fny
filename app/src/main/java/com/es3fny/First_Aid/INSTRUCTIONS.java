package com.es3fny.First_Aid;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.es3fny.R;


public class INSTRUCTIONS extends Fragment {

    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_instruction, container, false);

        ImageButton btnCPR = view.findViewById(R.id.cprbtn);
        btnCPR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(INSTRUCTIONS.this.getActivity(), cpr.class);
                startActivity(intent);
            }
        });
        ImageButton btnstress = view.findViewById(R.id.stress);
        btnstress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(INSTRUCTIONS.this.getActivity(), nerve.class);
                startActivity(intent);
            }
        });
        ImageButton btnwash = view.findViewById(R.id.wash);
        btnwash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(INSTRUCTIONS.this.getActivity(), wash.class);
                startActivity(intent);
            }
        });

        ImageButton btncall = view.findViewById(R.id.call);
        btncall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(INSTRUCTIONS.this.getActivity(), call_help.class);
                startActivity(intent);
            }
        });
        ImageButton btnsafe = view.findViewById(R.id.safety);
        btnsafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(INSTRUCTIONS.this.getActivity(), safe_position.class);
                startActivity(intent);
            }
        });
        return view;
    }
}
