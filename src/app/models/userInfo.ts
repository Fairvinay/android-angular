import { Account } from "./account";
import { Role } from "./types";

export class UserInfo {
  id?: string;
  accountId?: string;
  account?: Account;
  email?: string;
  password?: string;
  role?: Role;
  confirmed?: boolean;
  tfa?: boolean;
}
