# git-project-ianlieberman

1. I coded stage but honestly I don't know if it works as intended. It does make the file blobs and rehashes the higher directories.
2. Commit seems to work correctly.
3. Unfortunately I did not get to checkout :( seems like a lot of work and possibly dangerous
4. I fixed a lot of bugs in the existing code, which included writing to the same file in objects multiple times and not adding tree entries to the index file. There may still be a bug where if I have two files with the same name but different paths, my snapshot won't recognize both of them.
