module Main (main) where

import System.Environment (getArgs, getProgName)
import System.IO (hPutStrLn, stderr)
import System.Process (callProcess)

main :: IO ()
main = do
  args <- getArgs
  case args of
   "kill" : unit : rest ->
     docker $ "kill" : escapeUnit unit : rest
   "rm" : unit : rest ->
     docker $ "rm" : escapeUnit unit : rest
   "run" : unit : rest ->
     docker $ "run" : "--name" : escapeUnit unit : rest
   "stop" : unit : rest ->
     docker $ "stop" : escapeUnit unit : rest
   "start" : unit : rest ->
     docker $ "start" : escapeUnit unit : rest
   _ -> do
     progName <- getProgName
     hPutStrLn stderr $ "usage: " ++ progName ++
       " (run|start|stop|kill|rm) <unit name> [args...]\n" ++
       "Translates the commands to equivalent Docker commands\n\n" ++
       "Other commands are forwarded to Docker directly"

docker :: [String] -> IO ()
docker = callProcess "/usr/bin/docker"

escapeUnit :: String -> String
escapeUnit n =
  "unit_" ++ map replaceChar n
  where
    replaceChar c
      | c == '_' || c == '.' || c == '-' ||
        (c >= 'a' && c <= 'z') ||
        (c >= 'A' && c <= 'Z') ||
        (c >= '0' && c <= '9') = c
      | otherwise = '_'
