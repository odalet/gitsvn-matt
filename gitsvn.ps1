param (
    [string]$url,
    [string]$username,
    [string]$password,
    [string]$certAcceptResponse, # Should be t or p
    [string]$options,
    # In seconds. NB: this is *not*  a global timeout. This is
    # the maximum amount of time that we accept without receiving
    # anything from the console (in GitSvnCloneWrapper).
    [int]$timeout = 60 # default: 1 mn
)

# $logfile = "c:\temp\svngit.log" # for tests
$logfile = New-TemporaryFile

# Throw if Powershell Core
if ($PSVersionTable.PSEdition -eq "core") {
    throw "This script is incompatible with PowerShell Core"
}

try {
    $command = ""
    if ($options -eq "") {
        $command = "`{ $PSScriptRoot\gitsvn-wrapper.ps1 -url $url -username $username -password $password -certAcceptResponse $certAcceptResponse -logfile $logfile -timeout $timeout `}"
    }
    else {
        $command = "`{ $PSScriptRoot\gitsvn-wrapper.ps1 -url $url -username $username -password $password -certAcceptResponse $certAcceptResponse -logfile $logfile -options $options -timeout $timeout `}"
    }

    Write-Host "Execution in progress. It may take some time before some output appears. Please wait..."
    Invoke-Expression "powershell -Command ${command}"
    Get-Content $logfile
}
finally {
    if (Test-Path $logfile) {
        Remove-Item $logfile
    }
}
