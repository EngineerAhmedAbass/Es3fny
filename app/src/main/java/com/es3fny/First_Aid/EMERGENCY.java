package com.es3fny.First_Aid;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.es3fny.R;

public class EMERGENCY extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emergency, container, false);

        ImageButton btnBURN = view.findViewById(R.id.ImageButton01);
        btnBURN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EMERGENCY.this.getActivity(), Burn.class);
                startActivity(intent);

            }
        });

        ImageButton btnamputation = view.findViewById(R.id.ImageButton02);
        btnamputation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EMERGENCY.this.getActivity(), amputation.class);
                startActivity(intent);
            }
        });

        ImageButton btnasthma = view.findViewById(R.id.ImageButton03);
        btnasthma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EMERGENCY.this.getActivity(), asthma.class);
                startActivity(intent);
            }
        });

        ImageButton btnbleeding = view.findViewById(R.id.ImageButton04);
        btnbleeding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EMERGENCY.this.getActivity(), bleeding.class);
                startActivity(intent);
            }
        });

        ImageButton btnchoking = view.findViewById(R.id.ImageButton05);
        btnchoking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EMERGENCY.this.getActivity(), chocking.class);
                startActivity(intent);
            }
        });

        ImageButton btndogbite = view.findViewById(R.id.ImageButton06);
        btndogbite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EMERGENCY.this.getActivity(), dog_bite.class);
                startActivity(intent);
            }
        });

        ImageButton btnbaby_choking = view.findViewById(R.id.ImageButton07);
        btnbaby_choking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EMERGENCY.this.getActivity(), baby_choking.class);
                startActivity(intent);
            }
        });

        ImageButton btnchest_pain = view.findViewById(R.id.ImageButton08);
        btnchest_pain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EMERGENCY.this.getActivity(), chest_pain.class);
                startActivity(intent);
            }
        });

        ImageButton btnfever = view.findViewById(R.id.ImageButton09);
        btnfever.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EMERGENCY.this.getActivity(), fever.class);
                startActivity(intent);
            }
        });

        ImageButton btnFracture = view.findViewById(R.id.ImageButton10);
        btnFracture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EMERGENCY.this.getActivity(), fracture.class);
                startActivity(intent);
            }
        });

        ImageButton btncuts = view.findViewById(R.id.ImageButton11);
        btncuts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EMERGENCY.this.getActivity(), cuts.class);
                startActivity(intent);
            }
        });

        ImageButton nose = view.findViewById(R.id.ImageButton12);
        nose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EMERGENCY.this.getActivity(), nose_bleed.class);
                startActivity(intent);
            }
        });

        ImageButton drowning = view.findViewById(R.id.ImageButton13);
        drowning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EMERGENCY.this.getActivity(), drowning.class);
                startActivity(intent);
            }
        });

        ImageButton snake = view.findViewById(R.id.ImageButton14);
        snake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EMERGENCY.this.getActivity(), snake_bite.class);
                startActivity(intent);
            }
        });

        ImageButton stings = view.findViewById(R.id.ImageButton15);
        stings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EMERGENCY.this.getActivity(), bee_wasp_sting.class);
                startActivity(intent);
            }
        });

        ImageButton stroke = view.findViewById(R.id.ImageButton16);
        stroke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EMERGENCY.this.getActivity(), stroke.class);
                startActivity(intent);
            }
        });

        ImageButton epilepsy = view.findViewById(R.id.ImageButton18);
        epilepsy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EMERGENCY.this.getActivity(), epilepsy.class);
                startActivity(intent);
            }
        });

        ImageButton poisoning = view.findViewById(R.id.ImageButton20);
        poisoning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EMERGENCY.this.getActivity(), poisoning.class);
                startActivity(intent);
            }
        });
        return view;
    }

}


