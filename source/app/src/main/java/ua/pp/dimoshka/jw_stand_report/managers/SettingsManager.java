package ua.pp.dimoshka.jw_stand_report.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SettingsManager implements ISettingsManager {
    private static final String Key_PhoneNumbers = "PhoneNumbers";
    private static final String Key_EmailAddresses = "EmailAddresses";

    private final SharedPreferences preferences;

    public SettingsManager(Context context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public HashMap<Integer, String> getPhoneNumbers() {
        int[] idList = getIdList(Key_PhoneNumbers);
        HashMap<Integer, String> result = new HashMap<>();
        for (int id : idList) {
            String phoneKey = formatKey(id, Key_PhoneNumbers);
            String phone = preferences.getString(phoneKey, null);
            if (phone == null)
                throw new RuntimeException();
            result.put(id, phone);
        }
        return result;
    }

    @Override
    public void setPhoneNumbers(HashMap<Integer, String> phoneNumbers) {
        SharedPreferences.Editor editor = preferences.edit();

        setIdList(editor, phoneNumbers.keySet(), Key_PhoneNumbers);

        List<Map.Entry<Integer, String>> entriesList = new ArrayList<>(phoneNumbers.entrySet());

        for (Map.Entry<Integer, String> entry : entriesList) {
            int id = entry.getKey();
            String phoneKey = formatKey(id, Key_PhoneNumbers);
            String phoneNumber = entry.getValue();
            editor.putString(phoneKey, phoneNumber);
        }

        editor.apply();
    }

    @Override
    public HashMap<Integer, String> getEmailAddresses() {
        int[] idList = getIdList(Key_EmailAddresses);
        HashMap<Integer, String> result = new HashMap<>();
        for (int id : idList) {
            String emailKey = formatKey(id, Key_EmailAddresses);
            String email = preferences.getString(emailKey, null);
            if (email == null)
                throw new RuntimeException();
            result.put(id, email);
        }
        return result;
    }

    @Override
    public void setEmailAddresses(HashMap<Integer, String> emailAddresses) {
        SharedPreferences.Editor editor = preferences.edit();

        setIdList(editor, emailAddresses.keySet(), Key_EmailAddresses);

        List<Map.Entry<Integer, String>> entriesList = new ArrayList<>(emailAddresses.entrySet());

        for (Map.Entry<Integer, String> entry : entriesList) {
            int id = entry.getKey();
            String phoneKey = formatKey(id, Key_EmailAddresses);
            String phoneNumber = entry.getValue();
            editor.putString(phoneKey, phoneNumber);
        }

        editor.apply();
    }

    private int[] getIdList(String type) {
        Set<String> stringSet = preferences.getStringSet(type + "_idList", new HashSet<String>());
        String[] stringArray = stringSet.toArray(new String[stringSet.size()]);
        int[] idList = new int[stringArray.length];
        for (int i = 0; i < stringArray.length; i++) {
            idList[i] = Integer.parseInt(stringArray[i]);
        }
        return idList;
    }

    private void setIdList(SharedPreferences.Editor editor, int[] idList, String type) {
        Set<String> stringList = new HashSet<>();
        for (int id : idList)
            stringList.add(String.valueOf(id));
        editor.putStringSet(type + "_idList", stringList);
    }

    private void setIdList(SharedPreferences.Editor editor, Set<Integer> idSet, String type) {
        Integer[] idArray = idSet.toArray(new Integer[idSet.size()]);
        int[] idList = new int[idArray.length];
        for (int i = 0; i < idArray.length; i++) {
            idList[i] = idArray[i];
        }
        setIdList(editor, idList, type);
    }

    private String formatKey(int index, String type) {
        return String.format("%s_%s", type, index);
    }

}
