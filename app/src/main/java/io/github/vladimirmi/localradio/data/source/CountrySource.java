package io.github.vladimirmi.localradio.data.source;

import android.content.Context;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.github.vladimirmi.localradio.data.models.Country;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

@Singleton
public class CountrySource {

    private List<Country> countries = Collections.emptyList();

    @SuppressWarnings("WeakerAccess")
    @Inject
    public CountrySource(Context context) {
//        try (BufferedReader br = new BufferedReader(new InputStreamReader(
//                context.getResources().openRawResource(R.raw.countries)))) {
//            StringBuilder sb = new StringBuilder();
//            String line = br.readLine();
//
//            while (line != null) {
//                sb.append(line);
//                line = br.readLine();
//            }
//
//            Moshi moshi = Scopes.getAppScope().getInstance(Moshi.class);
//            JsonAdapter<List<Country>> adapter =
//                    moshi.adapter(Types.newParameterizedType(List.class, Country.class));
//            countries = adapter.fromJson(sb.toString());
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public List<Country> getCountries() {
        return countries;
    }
}
