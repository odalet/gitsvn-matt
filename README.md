# A wrapper around git svn command

This wrapper allows to 'remote control' `git svn` when it asks for user input on Windows.

It's made of a pile of hacks but seems to _kinda_ work. Should be thoroughly tested in real condition though!

## Usage

```powershell
.\gitsvn.ps1 -url https://my.svn.server/svn/project -username foo -password bar -certAcceptResponse t
```

Remarks:

* Pass `t` to temporarily accept an invalid certificate or `p` to accept it permanently.
* This script is not compatible (yet) with PowerShell Core. You'll need to run it using the regular `powershell.exe` that comes with Windows.
* There is no way to specify the directory into which to clone. It always clone in the current directory (hopefully not in the script directory).
* Note that the password is not leaked in the log file (because `git` does not echo it when it reads it from `stdin`).
* When deployed, one only needs the following files in the same directory (the other files, such as the Visual Studio solution or the C# project are only here for tests purpose):
  * `gitsvn.ps1`
  * `gitsvn-wrapper.ps1`
  * `GitSvnCloneWrapper.cs`

## How it works

`GitSvnCloneWrapper.cs` contains the logic that remote controls the `git` command. It relies on a huge copy/paste (and adaptation) of code found [here](https://github.com/LeeHolmes/await).

The first thing it does is create an instance of `AwaitDriver`. This class knows how to execute powershell in a console and then 'steal' this console handles and observe its inputs and outputs. It can also 'send' text to its input. Once `AwaitDriver` is created, we use it to send the `git` command then loop indefinitely (well until we detect a special marker that indicates the end of the `git` command). By examining the text that is output by `git`, we know when to tell it how to accept an invalid certificate or when to pass the user password.

In order to detect the end of the `git` command, we wrap it in a minimalist powershell code that will emit a guid and the command return code once it ends. This allows us to look for a very specific string and then know the command completed and at the same time retrieve its return code. Something like that:

```powershell
$marker='a426e3cc-e164-4125-a677-9b64414f606a';git svn clone...;"${marker}#$LASTEXITCODE#"
```

This is but only the first part of the story: along with a `Program.cs` we can now execute our `git svn clone` command. But we don't want to have to compile the project or provide a binary distribution and we want all of this to be run from a powershell script.

Here comes `gitsvn-wrapper.ps1`. This script takes a few arguments on the command line, compiles `GitSvnCloneWrapper.cs` on the fly and then execute it. Unfortunately, we can't run it directly from a powershell prompt as its console is being hacked by `AwaitDriver`.

This is why we need a 2nd powershell script: `gitsvn.ps1`. It takes pretty much the same arguments on the command line (apart from the log file), spawns a powershell instance that will run `gitsvn-wrapper.ps1` and retrieves its results by using a temporary file passed to `gitsvn-wrapper.ps1` for it to log its execution.
