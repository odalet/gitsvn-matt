namespace GitSvnWrapper
{
    internal static class Program
    {
        private static void Main(string[] args)
        {
            var wrapper = new GitSvnCloneWrapper();
            wrapper.Execute(
                args[0], // url
                args[1], // username
                args[2], // password
                args[3]  // t or p (temporarily or permanently accept the certificate)
            );
        }
    }
}
