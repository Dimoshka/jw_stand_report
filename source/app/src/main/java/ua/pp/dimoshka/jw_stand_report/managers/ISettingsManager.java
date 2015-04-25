package ua.pp.dimoshka.jw_stand_report.managers;

import java.util.HashMap;

public interface ISettingsManager {
    HashMap<Integer, String> getPhoneNumbers();

    void setPhoneNumbers(HashMap<Integer, String> phoneNumbers);

    HashMap<Integer, String> getEmailAddresses();

    void setEmailAddresses(HashMap<Integer, String> emailAddresses);
}
