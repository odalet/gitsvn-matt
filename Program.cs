namespace GitSvnWrapper
{
    internal static class Program
    {
        private static void Main(string[] args)
        {
            var wrapper = new GitSvnCloneWrapper();

            var hasOptions = args.Length > 4;
            if (hasOptions) wrapper.Execute(
                    args[0], // url
                    args[1], // username
                    args[2], // password
                    args[3], // t or p (temporarily or permanently accept the certificate)
                    60, // timeout
                    args[4] // options (appended to the end of the git command)
                );
            else wrapper.Execute(
                    args[0], // url
                    args[1], // username
                    args[2], // password
                    args[3],  // t or p (temporarily or permanently accept the certificate)
                    60 // timeout
                );
        }
    }
}
