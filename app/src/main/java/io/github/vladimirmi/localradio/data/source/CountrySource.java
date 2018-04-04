package io.github.vladimirmi.localradio.data.source;

import android.content.Context;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.Country;
import io.github.vladimirmi.localradio.di.Scopes;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class CountrySource {

    private Context context;
    private List<Country> countries;

    @Inject
    public CountrySource(Context context) {
        this.context = context;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                context.getResources().openRawResource(R.raw.countries)))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }

            Moshi moshi = Scopes.getAppScope().getInstance(Moshi.class);
            JsonAdapter<List<Country>> adapter =
                    moshi.adapter(Types.newParameterizedType(List.class, Country.class));
            countries = adapter.fromJson(sb.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Country> getCountries() {
        return countries;
    }
}
