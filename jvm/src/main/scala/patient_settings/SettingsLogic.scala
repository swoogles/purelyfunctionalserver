package patient_settings

import auth.Sub

import scala.util.Try

case class Preference(
  name: String
)

case class UserSetting(
  preference: Preference,
  user: Sub
)

case class UserSettingWithValue(
  preference: Preference,
  user: Sub,
  value: String
)

case class UserSettingWithDefaultValue(
  preference: Preference,
  value: String
) {

  def forUser(user: Sub) =
    UserSettingWithValue(preference, user, value)
}

trait SettingsLogic {
  def getValueFor(patient: Preference, user: Sub): Option[UserSettingWithValue]
  def getDefaultValueFor__unsafe(preference: Preference): UserSettingWithDefaultValue

  def saveValue(patientSettingWithValue: UserSettingWithValue): UserSettingWithValue
}
