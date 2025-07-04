package com.hbb20;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by hbb20 on 11/1/16.
 */
class CountryCodeAdapter extends RecyclerView.Adapter<CountryCodeAdapter.CountryCodeViewHolder> implements SectionTitleProvider {
    List<CCPCountry> filteredCountries = null, masterCountries = null;
    TextView textView_noResult;
    CountryCodePicker codePicker;
    LayoutInflater inflater;
    EditText editText_search;
    Dialog dialog;
    Context context;
    RelativeLayout rlQueryHolder;
    ImageView imgClearQuery;
    int preferredCountriesCount = 0;

    CountryCodeAdapter(Context context, List<CCPCountry> countries, CountryCodePicker codePicker, RelativeLayout rlQueryHolder, final EditText editText_search, TextView textView_noResult, Dialog dialog, ImageView imgClearQuery) {
        this.context = context;
        this.masterCountries = countries;
        this.codePicker = codePicker;
        this.dialog = dialog;
        this.textView_noResult = textView_noResult;
        this.editText_search = editText_search;
        this.rlQueryHolder = rlQueryHolder;
        this.imgClearQuery = imgClearQuery;
        this.inflater = LayoutInflater.from(context);
        this.filteredCountries = getFilteredCountries("");
        setSearchBar();
    }

    private void setSearchBar() {
        if (codePicker.isSearchAllowed()) {
            imgClearQuery.setVisibility(View.GONE);
            setTextWatcher();
            setQueryClearListener();
        } else {
            rlQueryHolder.setVisibility(View.GONE);
        }
    }

    private void setQueryClearListener() {
        imgClearQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText_search.setText("");
            }
        });
    }

    /**
     * add textChangeListener, to apply new query each time editText get text changed.
     */
    private void setTextWatcher() {
        if (this.editText_search != null) {
            this.editText_search.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applyQuery(s.toString());
                    if (s.toString().trim().equals("")) {
                        imgClearQuery.setVisibility(View.GONE);
                    } else {
                        imgClearQuery.setVisibility(View.VISIBLE);
                    }
                }
            });

            this.editText_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        InputMethodManager in = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        in.hideSoftInputFromWindow(editText_search.getWindowToken(), 0);
                        return true;
                    }

                    return false;
                }
            });
        }
    }

    /**
     * Filter country list for given keyWord / query.
     * Lists all countries that contains @param query in country's name, name code or phone code.
     *
     * @param query : text to match against country name, name code or phone code
     */
    private void applyQuery(String query) {


        textView_noResult.setVisibility(View.GONE);
        query = query.toLowerCase();

        //if query started from "+" ignore it
        if (query.length() > 0 && query.charAt(0) == '+') {
            query = query.substring(1);
        }

        filteredCountries = getFilteredCountries(query);

        if (filteredCountries.size() == 0) {
            textView_noResult.setVisibility(View.VISIBLE);
        }
        notifyDataSetChanged();
    }

    private List<CCPCountry> getFilteredCountries(String query) {
        List<CCPCountry> tempCCPCountryList = new ArrayList<CCPCountry>();
        preferredCountriesCount = 0;
        if (codePicker.preferredCountries != null && codePicker.preferredCountries.size() > 0) {
            for (CCPCountry CCPCountry : codePicker.preferredCountries) {
                if (CCPCountry.isEligibleForQuery(query)) {
                    tempCCPCountryList.add(CCPCountry);
                    preferredCountriesCount++;
                }
            }

            if (tempCCPCountryList.size() > 0) { //means at least one preferred country is added.
                CCPCountry divider = null;
                tempCCPCountryList.add(divider);
                preferredCountriesCount++;
            }
        }

        for (CCPCountry CCPCountry : masterCountries) {
            if (CCPCountry.isEligibleForQuery(query)) {
                tempCCPCountryList.add(CCPCountry);
            }
        }
        return tempCCPCountryList;
    }

    @Override
    public CountryCodeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View rootView = inflater.inflate(R.layout.layout_recycler_country_tile, viewGroup, false);
        CountryCodeViewHolder viewHolder = new CountryCodeViewHolder(rootView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CountryCodeViewHolder countryCodeViewHolder, final int i) {
        countryCodeViewHolder.setCountry(filteredCountries.get(i));
        if (filteredCountries.size() > i && filteredCountries.get(i) != null) {
            countryCodeViewHolder.getMainView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (filteredCountries != null && filteredCountries.size() > i) {
                        codePicker.onUserTappedCountry(filteredCountries.get(i));
                    }
                    if (view != null && filteredCountries != null && filteredCountries.size() > i && filteredCountries.get(i) != null) {
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        dialog.dismiss();
                    }
                }
            });
        } else {
            countryCodeViewHolder.getMainView().setOnClickListener(null);
        }

    }

    @Override
    public int getItemCount() {
        return filteredCountries.size();
    }

    @Override
    public String getSectionTitle(int position) {
        CCPCountry ccpCountry = filteredCountries.get(position);
        if (preferredCountriesCount > position) {
            return "★";
        } else if (ccpCountry != null) {
            return ccpCountry.getName().substring(0, 1);
        } else {
            return "☺"; //this should never be the case
        }
    }

    class CountryCodeViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout relativeLayout_main,rootRowCountryTile;
        TextView textView_name, textView_code;
        ImageView imageViewFlag;
        CardView cardViewFlagHolder;
        View divider;

        public CountryCodeViewHolder(View itemView) {
            super(itemView);
            relativeLayout_main = (RelativeLayout) itemView;

            textView_name = (TextView) relativeLayout_main.findViewById(R.id.textView_countryName);
            textView_code = (TextView) relativeLayout_main.findViewById(R.id.textView_code);
            imageViewFlag = (ImageView) relativeLayout_main.findViewById(R.id.image_flag);
            cardViewFlagHolder = (CardView) relativeLayout_main.findViewById(R.id.cardView_flag_holder);
            divider = relativeLayout_main.findViewById(R.id.preferenceDivider);

            if (codePicker.getDialogTextColor() != 0) {
                textView_name.setTextColor(codePicker.getDialogTextColor());
                textView_code.setTextColor(codePicker.getDialogTextColor());
                divider.setBackgroundColor(codePicker.getDialogTextColor());
            }

            if(codePicker.getCcpDialogRippleEnable()){
                TypedValue outValue = new TypedValue();
                context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                if(outValue.resourceId!=0)
                    relativeLayout_main.setBackgroundResource(outValue.resourceId);
                else
                    relativeLayout_main.setBackgroundResource(outValue.data);
            }

            try {
                if (codePicker.getDialogTypeFace() != null) {
                    if (codePicker.getDialogTypeFaceStyle() != CountryCodePicker.DEFAULT_UNSET) {
                        textView_code.setTypeface(codePicker.getDialogTypeFace(), codePicker.getDialogTypeFaceStyle());
                        textView_name.setTypeface(codePicker.getDialogTypeFace(), codePicker.getDialogTypeFaceStyle());
                    } else {
                        textView_code.setTypeface(codePicker.getDialogTypeFace());
                        textView_name.setTypeface(codePicker.getDialogTypeFace());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setCountry(CCPCountry ccpCountry) {
            if (ccpCountry != null) {
                divider.setVisibility(View.GONE);
                textView_name.setVisibility(View.VISIBLE);
                textView_code.setVisibility(View.VISIBLE);
                if (codePicker.isCcpDialogShowPhoneCode()) {
                    textView_code.setVisibility(View.VISIBLE);
                } else {
                    textView_code.setVisibility(View.GONE);
                }

                String countryName = "";

                if (codePicker.getCcpDialogShowFlag() && codePicker.ccpUseEmoji) {
                    //extra space is just for alignment purpose
                    countryName += CCPCountry.getFlagEmoji(ccpCountry) + "   ";
                }

                countryName += ccpCountry.getName();

                if (codePicker.getCcpDialogShowNameCode()) {
                    countryName += " (" + ccpCountry.getNameCode().toUpperCase(Locale.US) + ")";
                }

                textView_name.setText(countryName);
                textView_code.setText("+" + ccpCountry.getPhoneCode());

                if (!codePicker.getCcpDialogShowFlag() || codePicker.ccpUseEmoji) {
                    cardViewFlagHolder.setVisibility(View.GONE);
                } else {
                    cardViewFlagHolder.setVisibility(View.VISIBLE);
                    imageViewFlag.setImageResource(ccpCountry.getFlagID());
                }
            } else {
                divider.setVisibility(View.VISIBLE);
                textView_name.setVisibility(View.GONE);
                textView_code.setVisibility(View.GONE);
                cardViewFlagHolder.setVisibility(View.GONE);
            }
        }

        public RelativeLayout getMainView() {
            return relativeLayout_main;
        }
    }
}

