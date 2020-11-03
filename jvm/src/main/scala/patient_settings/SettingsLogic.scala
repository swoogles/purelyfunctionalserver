package patient_settings

import com.billding.settings.{Setting, SettingWithValue, Sub, UserSettingWithValue}

trait SettingsLogic {
  def getValueFor(setting: Setting, user: Sub): Option[UserSettingWithValue]
  def getDefaultValueFor__unsafe(setting: Setting): SettingWithValue

  def saveValue(patientSettingWithValue: UserSettingWithValue): UserSettingWithValue
}
