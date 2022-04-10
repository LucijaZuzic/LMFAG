package com.example.lmfag.utility;

import com.example.lmfag.R;

public class EventTypeToDrawable {
    public static int getEventTypeToDrawable(String eventType) {
        switch (eventType) {
            case "skiing":
                return R.drawable.ic_baseline_downhill_skiing_24;
            case "hiking":
                return R.drawable.ic_baseline_hiking_24;
            case "ice skating":
                return R.drawable.ic_baseline_ice_skating_24;
            case "kayaking":
                return R.drawable.ic_baseline_kayaking_24;
            case "kitesurfing":
                return R.drawable.ic_baseline_kitesurfing_24;
            case "nordic walking":
                return R.drawable.ic_baseline_nordic_walking_24;
            case "paragliding":
                return R.drawable.ic_baseline_paragliding_24;
            case "biking":
                return R.drawable.ic_baseline_pedal_bike_24;
            case "roller skating":
                return R.drawable.ic_baseline_roller_skating_24;
            case "rowing":
                return R.drawable.ic_baseline_rowing_24;
            case "sailing":
                return R.drawable.ic_baseline_sailing_24;
            case "scuba diving":
                return R.drawable.ic_baseline_scuba_diving_24;
            case "yoga":
                return R.drawable.ic_baseline_self_improvement_24;
            case "skateboarding":
                return R.drawable.ic_baseline_skateboarding_24;
            case "sledding":
                return R.drawable.ic_baseline_sledding_24;
            case "snowboarding":
                return R.drawable.ic_baseline_snowboarding_24;
            case "baseball":
                return R.drawable.ic_baseline_sports_baseball_24;
            case "basketball":
                return R.drawable.ic_baseline_sports_basketball_24;
            case "cricket":
                return R.drawable.ic_baseline_sports_cricket_24;
            case "esports":
                return R.drawable.ic_baseline_sports_esports_24;
            case "football":
                return R.drawable.ic_baseline_sports_football_24;
            case "golf":
                return R.drawable.ic_baseline_sports_golf_24;
            case "gymnastics":
                return R.drawable.ic_baseline_sports_gymnastics_24;
            case "handball":
                return R.drawable.ic_baseline_sports_handball_24;
            case "hockey":
                return R.drawable.ic_baseline_sports_hockey_24;
            case "kabaddi":
                return R.drawable.ic_baseline_sports_kabaddi_24;
            case "martial arts":
                return R.drawable.ic_baseline_sports_martial_arts_24;
            case "MMA":
                return R.drawable.ic_baseline_sports_mma_24;
            case "motorsports":
                return R.drawable.ic_baseline_sports_motorsports_24;
            case "rugby":
                return R.drawable.ic_baseline_sports_rugby_24;
            case "soccer":
                return R.drawable.ic_baseline_sports_soccer_24;
            case "tennis":
                return R.drawable.ic_baseline_sports_tennis_24;
            case "volleyball":
                return R.drawable.ic_baseline_sports_volleyball_24;
            case "dice games":
                return R.drawable.ic_baseline_casino_24;
            case "card games":
                return R.drawable.ic_baseline_content_copy_24;
            case "tabletop games":
                return R.drawable.ic_baseline_table_restaurant_24;
            case "poker":
                return R.drawable.ic_baseline_favorite_24;
            default:
                return R.drawable.ic_baseline_interests_24;
        }
    }
}
