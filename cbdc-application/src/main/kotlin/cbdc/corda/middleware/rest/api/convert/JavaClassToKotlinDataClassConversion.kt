package cbdc.corda.middleware.rest.api.convert

import cbdc.corda.flow.NewVaultK
import cbdc.corda.flow.NewWalletK
import cbdc.corda.state.AuthorizationProps
import cbdc.corda.state.AuthorizationState
import cbdc.corda.state.CertInfoK
import cbdc.corda.state.LimitK
import cbdc.corda.state.MintAndBurnProps
import cbdc.corda.state.NewNumberDataK
import cbdc.corda.state.NumberState
import cbdc.corda.state.OtpDataK
import cbdc.corda.state.PlanState
import cbdc.corda.state.TransferProps
import cbdc.corda.state.VaultSettingK
import cbdc.corda.state.VaultStatusK
import cbdc.corda.state.WalletSettingK
import cbdc.corda.state.WalletStatusK
import cbdc.corda.state.WalletTypeK
import gov.cbc.cbdc.utilities.domain.entity.CertInfo
import gov.cbc.cbdc.utilities.domain.entity.Limit
import gov.cbc.cbdc.utilities.domain.entity.NewNumberData
import gov.cbc.cbdc.utilities.domain.entity.NumberData
import gov.cbc.cbdc.utilities.domain.entity.OtpData
import gov.cbc.cbdc.utilities.domain.entity.TransferAttribute
import gov.cbc.cbdc.utilities.domain.entity.account.NewVault
import gov.cbc.cbdc.utilities.domain.entity.account.NewWallet
import gov.cbc.cbdc.utilities.domain.entity.account.VaultSetting
import gov.cbc.cbdc.utilities.domain.entity.account.VaultStatus
import gov.cbc.cbdc.utilities.domain.entity.account.WalletSetting
import gov.cbc.cbdc.utilities.domain.entity.account.WalletStatus
import gov.cbc.cbdc.utilities.domain.entity.authorization.Authorization
import gov.cbc.cbdc.utilities.domain.entity.plan.Plan
import gov.cbc.cbdc.utilities.domain.enums.WalletType

fun NewVault.toKotlin() = NewVaultK(vaultCertInfo?.toKotlin(), agencyCertInfo?.toKotlin())
fun NewWallet.toKotlin() = NewWalletK(dn, balanceLimit, phoneNumber, type.toKotlin(), mcc, certTxnLimit, keyTxnLimit, certInfo.toKotlin(), pubKey)
fun WalletType.toKotlin() = if (this == WalletType.ANONYMOUS) WalletTypeK.ANONYMOUS else WalletTypeK.REGISTERED
fun Limit.toKotlin() = LimitK(balanceLimit, certTxnLimit, keyTxnLimit)
fun OtpData.toKotlin() = OtpDataK(otp, createTime)
fun NewNumberData.toKotlin() = NewNumberDataK(walletID, cvc, amount)
fun Plan.toKotlin() = PlanState(operation, amountLimit, certAllow, keyAllow, remark)

fun WalletStatusK.toJava() = WalletStatus(id, dn, balance, balanceLimit, totalCount, statusLastModified)
fun VaultStatusK.toJava() = VaultStatus(id, balance, totalCount, statusLastModified)
fun VaultSettingK.toJava() = VaultSetting(id, vaultCertInfo?.toJava(), agencyCertInfo?.toJava(), seedKey, frozen, disabled, createTime, settingLastModified)
fun WalletSettingK.toJava() = WalletSetting(id, phoneNumber, typeK?.toJava(), mcc, certTxnLimit, keyTxnLimit, certInfo.toJava(), pubKey, divData, frozen, disabled, listOf(), createTime, settingLastModified)
fun WalletTypeK.toJava() = if (this == WalletTypeK.ANONYMOUS) WalletType.ANONYMOUS else WalletType.REGISTERED
fun OtpDataK.toJava() = OtpData(otp, createTime)
fun NumberState.toJava() = NumberData(id, walletId, cvc, amount, createTime)
fun PlanState.toJava() = Plan(operation, listOf(), listOf(), amountLimit, certAllow, keyAllow, remark)

fun CertInfoK.toJava() = CertInfo(certID, pubKey, notAfter)
fun CertInfo.toKotlin() = CertInfoK(certID, pubKey, notAfter)

fun TransferAttribute.toTransferProps() = TransferProps(authorizedAgencyID, cvc, won, remark, paymentMethod, insID)
fun TransferAttribute.toMintAndBurnProps() = MintAndBurnProps(remark)

fun Authorization.toKotlin() = AuthorizationProps(
    this.authID,
    this.operation,
    this.senderID,
    this.authorizedAgencyID,
    this.recipientID,
    this.remark,
    this.voidRemark
)
fun AuthorizationState.toJava() = Authorization(
    this.authId,
    this.operation,
    this.senderId,
    this.authorizedAgencyId,
    this.recipientId,
    this.remark,
    this.voidRemark,
    this.status,
    this.createTime
)
