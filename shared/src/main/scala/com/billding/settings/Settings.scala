package com.billding.settings

import scala.util.Try

case class Sub(id: String) // TODO Figure out if this is a good place for this class

case class Setting(
  name: String
)

case class UserSetting(
  setting: Setting,
  user: Sub
)

case class UserSettingWithValue(
  setting: Setting,
  user: Sub,
  value: String
)

case class SettingWithValue(
  setting: Setting,
  value: String
) {

  def forUser(user: Sub) =
    UserSettingWithValue(setting, user, value)
}
