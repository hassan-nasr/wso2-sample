namespace java org.wso2.carbon.bam.service

exception AuthenticationException {
    1: required string message
}

service AuthenticatorService {
   string authenticate(1:required string userName, 2:required string password) throws
                                                        (1:AuthenticationException ae)
}