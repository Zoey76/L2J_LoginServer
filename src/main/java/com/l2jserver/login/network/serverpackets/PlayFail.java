/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.login.network.serverpackets;

public final class PlayFail extends L2LoginServerPacket
{
	public static enum PlayFailReason
	{
		REASON_NO_MESSAGE(0x00),
		REASON_SYSTEM_ERROR_LOGIN_LATER(0x01),
		REASON_USER_OR_PASS_WRONG(0x02),
		REASON_ACCESS_FAILED_TRY_AGAIN_LATER(0x04),
		REASON_ACCOUNT_INFO_INCORRECT_CONTACT_SUPPORT(0x05),
		REASON_ACCOUNT_IN_USE(0x07),
		REASON_UNDER_18_YEARS_KR(0x0C),
		REASON_SERVER_OVERLOADED(0x0F),
		REASON_SERVER_MAINTENANCE(0x10),
		REASON_TEMP_PASS_EXPIRED(0x11),
		REASON_GAME_TIME_EXPIRED(0x12),
		REASON_NO_TIME_LEFT(0x13),
		REASON_SYSTEM_ERROR(0x14),
		REASON_ACCESS_FAILED(0x15),
		REASON_RESTRICTED_IP(0x16),
		REASON_WEEK_USAGE_FINISHED(0x1E),
		REASON_SECURITY_CARD_NUMBER_INVALID(0x1F),
		REASON_AGE_NOT_VERIFIED_CANT_LOG_BEETWEEN_10PM_6AM(0x20),
		REASON_SERVER_CANNOT_BE_ACCESSED_BY_YOUR_COUPON(0x21),
		REASON_DUAL_BOX(0x23),
		REASON_INACTIVE(0x24),
		REASON_USER_AGREEMENT_REJECTED_ON_WEBSITE(0x25),
		REASON_GUARDIAN_CONSENT_REQUIRED(0x26),
		REASON_USER_AGREEMENT_DECLINED_OR_WITHDRAWL_REQUEST(0x27),
		REASON_ACCOUNT_SUSPENDED_CALL(0x28),
		REASON_CHANGE_PASSWORD_AND_QUIZ_ON_WEBSITE(0x29),
		REASON_ALREADY_LOGGED_INTO_10_ACCOUNTS(0x2A),
		REASON_MASTER_ACCOUNT_RESTRICTED(0x2B),
		REASON_CERTIFICATION_FAILED(0x2E),
		REASON_TELEPHONE_CERTIFICATION_UNAVAILABLE(0x2F),
		REASON_TELEPHONE_SIGNALS_DELAYED(0x30),
		REASON_CERTIFICATION_FAILED_LINE_BUSY(0x31),
		REASON_CERTIFICATION_SERVICE_NUMBER_EXPIRED_OR_INCORRECT(0x32),
		REASON_CERTIFICATION_SERVICE_CURRENTLY_BEING_CHECKED(0x33),
		REASON_CERTIFICATION_SERVICE_CANT_BE_USED_HEAVY_VOLUME(0x34),
		REASON_CERTIFICATION_SERVICE_EXPIRED_GAMEPLAY_BLOCKED(0x35),
		REASON_CERTIFICATION_FAILED_3_TIMES_GAMEPLAY_BLOCKED_30_MIN(0x36),
		REASON_CERTIFICATION_DAILY_USE_EXCEEDED(0x37),
		REASON_CERTIFICATION_UNDERWAY_TRY_AGAIN_LATER(0x38);
		
		private final int _code;
		
		PlayFailReason(int code)
		{
			_code = code;
		}
		
		public final int getCode()
		{
			return _code;
		}
	}
	
	private final PlayFailReason _reason;
	
	public PlayFail(PlayFailReason reason)
	{
		_reason = reason;
	}
	
	@Override
	protected void write()
	{
		writeC(0x06);
		writeC(_reason.getCode());
	}
}
