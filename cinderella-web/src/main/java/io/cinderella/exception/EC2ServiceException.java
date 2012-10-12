package io.cinderella.exception;


/**
 * @author shane
 * @since 9/27/12
 */
public class EC2ServiceException extends RuntimeException {
    private static final long serialVersionUID = 8857313467757867680L;

    // ServerError & ClientError are correct as of schema version 2010-08-31

    public static enum ServerError {
        InsufficientAddressCapacity("InsufficientAddressCapacity", 500),
        InsufficientInstanceCapacity("InsufficientInstanceCapacity", 500),
        InsufficientReservedInstanceCapacity("InsufficientReservedInstanceCapacity", 500),
        InternalError("InternalError", 500),
        Unavailable("Unavailable", 501);

        private String errorString;
        private int httpErrorCode;

        private ServerError(String errorString, int errorCode) {
            this.errorString = errorString;
            this.httpErrorCode = errorCode;
        }

        public String getErrorString() { return errorString; }
        public int getHttpErrorCode() {return httpErrorCode; }
    }

    public static enum ClientError {
        AddressLimitExceeded("AddressLimitExceeded", 400),
        AttachmentLimitExceeded("AttachmentLimitExceeded", 400),
        AuthFailure("AuthFailure", 400),
        Blocked("Blocked", 400),
        FilterLimitExceeded("FilterLimitExceeded", 400),
        IdempotentParameterMismatch("IdempotentParameterMismatch", 400),
        IncorrectState("IncorrectState", 400),
        InstanceLimitExceeded("InstanceLimitExceeded", 400),
        InsufficientInstanceCapacity("InsufficientInstanceCapacity", 400),
        InsufficientReservedInstancesCapacity("InsufficientReservedInstancesCapacity", 400),
        InvalidAMIAttributeItemValue("InvalidAMIAttributeItemValue", 400),
        InvalidAMIID_Malformed("InvalidAMIID.Malformed", 400),
        InvalidAMIID_NotFound("InvalidAMIID.NotFound", 400),
        InvalidAMIID_Unavailable("InvalidAMIID.Unavailable", 400),
        InvalidAttachment_NotFound("InvalidAttachment.NotFound", 400),
        InvalidDevice_InUse("InvalidDevice.InUse", 400),
        InvalidGroup_Duplicate("InvalidGroup.Duplicate", 400),
        InvalidGroup_InUse("InvalidGroup.InUse", 400),
        InvalidGroup_NotFound("InvalidGroup.NotFound", 400),
        InvalidGroup_Reserved("InvalidGroup.Reserved", 400),
        InvalidInstanceID_Malformed("InvalidInstanceID.Malformed", 400),
        InvalidInstanceID_NotFound("InvalidInstanceID.NotFound", 400),
        InvalidIPAddress_InUse("InvalidIPAddress.InUse", 400),
        InvalidKeyPair_Duplicate("InvalidKeyPair.Duplicate", 400),
        InvalidKeyPair_Format("InvalidKeyPair.Format", 400),
        InvalidKeyPair_NotFound("InvalidKeyPair.NotFound", 400),
        InvalidManifest("InvalidManifest", 400),
        InvalidParameterCombination("InvalidParameterCombination", 400),
        InvalidParameterValue("InvalidParameterValue", 400),
        InvalidPermission_Duplicate("InvalidPermission.Duplicate", 400),
        InvalidPermission_Malformed("InvalidPermission.Malformed", 400),
        InvalidReservationID_Malformed("InvalidReservationID.Malformed", 400),
        InvalidReservationID_NotFound("InvalidReservationID.NotFound", 400),
        InvalidSnapshotID_Malformed("InvalidSnapshotID.Malformed", 400),
        InvalidSnapshot_NotFound("InvalidSnapshot.NotFound", 400),
        InvalidUserID_Malformed("InvalidUserID.Malformed", 400),
        InvalidReservedInstancesId("InvalidReservedInstancesId", 400),
        InvalidReservedInstancesOfferingId("InvalidReservedInstancesOfferingId", 400),
        InvalidVolumeID_Duplicate("InvalidVolumeID.Duplicate", 400),
        InvalidVolumeID_Malformed("InvalidVolumeID.Malformed", 400),
        InvalidVolume_NotFound("InvalidVolume.NotFound", 400),
        InvalidVolumeID_ZoneMismatch("InvalidVolumeID.ZoneMismatch", 400),
        InvalidZone_NotFound("InvalidZone.NotFound", 400),
        NonEBSInstance("NonEBSInstance", 400),
        PendingVerification("PendingVerification", 400),
        PendingSnapshotLimitExceeded("PendingSnapshotLimitExceeded", 400),
        ReservedInstancesLimitExceeded("ReservedInstancesLimitExceeded", 400),
        SnapshotLimitExceeded("SnapshotLimitExceeded", 400),
        UnknownParameter("UnknownParameter", 400),
        Unsupported("Unsupported", 400),
        VolumeLimitExceeded("VolumeLimitExceeded", 400);

        private String errorString;
        private int httpErrorCode;

        private ClientError(String errorString, int errorCode) {
            this.errorString = errorString;
            this.httpErrorCode = errorCode;
        }

        public String getErrorString() { return errorString; }
        public int getHttpErrorCode() {return httpErrorCode; }
    }

    private int httpErrorCode = 0;
    private String errorCode;

    public EC2ServiceException() {
    }

    public EC2ServiceException(String message) {
        super(message);
    }

    public EC2ServiceException(Throwable e) {
        super(e);
    }

    public EC2ServiceException(String message, Throwable e) {
        super(message, e);
    }

    public EC2ServiceException(String message, int errorCode) {
//        super(message, new AxisFault(message, new QName("EC2Error")));
        super(message);
        this.httpErrorCode = errorCode;
    }

    public EC2ServiceException(String message, Throwable e, int errorCode) {
        super(message, e);
        this.httpErrorCode = errorCode;
    }

    public EC2ServiceException(ServerError errorCode, String message) {
//        super(message, new AxisFault(message, new QName(errorCode.getErrorString())));
        super(message);
        this.errorCode = errorCode.getErrorString();
        this.httpErrorCode = errorCode.getHttpErrorCode();
    }

    public EC2ServiceException(ClientError errorCode, String message) {
//        super(message, new AxisFault(message, new QName(errorCode.getErrorString())));
        super(message);
        this.errorCode = errorCode.getErrorString();
        this.httpErrorCode = errorCode.getHttpErrorCode();
    }

    public int getHttpErrorCode() {
        return this.httpErrorCode;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

}
